#!/usr/bin/python

# -*- coding: utf-8; -*-
#
# Copyright 2012 Red Hat, Inc. and/or its affiliates.
#
# Licensed under the Eclipse Public License version 1.0, available at
# http://www.eclipse.org/legal/epl-v10.html

import re
import sys
import os
from multiprocessing import Process
from utils import *
from xml.etree.ElementTree import ElementTree

try:
    from argparse import ArgumentParser
except:
    prettyprint('''
        Welcome to the Escalante Release Script.
        This release script requires that you use at least Python 2.7.0.  It appears
        that you do not have the collections.Counter available, which are available
        by default in Python 2.7.0.
        ''', Levels.FATAL)
    sys.exit(1)

modules = []
uploader = None
git = None

def get_modules(directory):
    '''Analyses the pom.xml file and extracts declared modules'''
    tree = ElementTree()
    f = directory + "/pom.xml"
    if settings.verbose:
        print "Parsing %s to get a list of modules in project" % f
    tree.parse(f)
    mods = tree.findall(".//{%s}module" % maven_pom_xml_namespace)
    for m in mods:
        modules.append(m.text)


def help_and_exit():
    prettyprint('''
        Welcome to the Escalante Release Script.
        
%s        Usage:%s
        
            $ bin/release.py <version> <branch to tag from>
            
%s        E.g.,%s
        
            $ bin/release.py 0.1.0 %s<-- this will tag off master.%s
            
            $ bin/release.py 2.0.0.CR1 2.x %s<-- this will use the appropriate branch.%s
            
    ''' % (
    Colors.yellow(), Colors.end_color(), Colors.yellow(), Colors.end_color(),
    Colors.green(), Colors.end_color(), Colors.green(), Colors.end_color()),
                Levels.INFO)
    sys.exit(0)


def validate_version(version):
    version_pattern = get_version_pattern()
    if version_pattern.match(version):
        return version.strip().upper()
    else:
        prettyprint("Invalid version '" + version + "'!\n", Levels.FATAL)
        help_and_exit()


def tag_release(version, branch):
    if git.remote_branch_exists():
        git.switch_to_branch()
        git.create_tag_branch()
    else:
        prettyprint(
            "Branch %s cannot be found on upstream repository.  Aborting!" % branch
            , Levels.FATAL)
        sys.exit(100)


def get_project_version_tag(tree):
    return tree.find("./{%s}version" % (maven_pom_xml_namespace))


def get_parent_version_tag(tree):
    return tree.find("./{%s}parent/{%s}version" % (
    maven_pom_xml_namespace, maven_pom_xml_namespace))


def get_properties_version_tag(tree):
    return tree.find("./{%s}properties/{%s}project-version" % (
    maven_pom_xml_namespace, maven_pom_xml_namespace))


def write_pom(tree, pom_file):
    tree.write("tmp.xml", 'UTF-8')
    in_f = open("tmp.xml")
    out_f = open(pom_file, "w")
    try:
        for l in in_f:
            newstr = l.replace("ns0:", "").replace(":ns0", "").replace("ns1",
                                                                       "xsi")
            out_f.write(newstr)
    finally:
        in_f.close()
        out_f.close()
        os.remove("tmp.xml")
    if settings.verbose:
        prettyprint(" ... updated %s" % pom_file, Levels.INFO)


def patch(pom_file, version):
    """Updates the version in a POM file.  We need to locate //project/parent/version,
    //project/version and //project/properties/project-version and replace the
    contents of these with the new version"""
    if settings.verbose:
        prettyprint("Patching %s" % pom_file, Levels.DEBUG)
    tree = ElementTree()
    tree.parse(pom_file)
    need_to_write = False

    tags = [get_project_version_tag(tree), get_properties_version_tag(tree)]
    # Skip JBoss parent version
    if pom_file != "./pom.xml":
        tags.append(get_parent_version_tag(tree))

    for tag in tags:
        if tag is not None:
            if settings.verbose:
                prettyprint(
                    "%s is %s.  Setting to %s" % (str(tag), tag.text, version),
                    Levels.DEBUG)
            tag.text = version
            need_to_write = True

    if need_to_write:
        # write to file again!
        write_pom(tree, pom_file)
        return True
    else:
        if settings.verbose:
            prettyprint("File doesn't need updating; nothing replaced!",
                        Levels.DEBUG)
        return False


def get_poms_to_patch(working_dir):
    get_modules(working_dir)
    if settings.verbose:
        prettyprint('Available modules are ' + str(modules), Levels.DEBUG)
    poms_to_patch = [working_dir + "/pom.xml"]
    for m in modules:
        poms_to_patch.append(working_dir + "/" + m + "/pom.xml")
        # Look for additional POMs that are not directly referenced!
    for additionalPom in GlobDirectoryWalker(working_dir, 'pom.xml'):
        if additionalPom not in poms_to_patch:
            poms_to_patch.append(additionalPom)

    return poms_to_patch


def update_versions(base_dir, version):
    os.chdir(base_dir)
    poms_to_patch = get_poms_to_patch(".")

    modified_files = []
    for pom in poms_to_patch:
        if patch(pom, version):
            modified_files.append(pom)

    ## Now look for Version.java
    version_java = "./modules/core/src/main/scala/io/escalante/Version.scala"
    modified_files.append(version_java)

    f_in = open(version_java)
    f_out = open(version_java + ".tmp", "w")

    regexp = re.compile('\s*val (MAJOR|MINOR|MICRO|MODIFIER|SNAPSHOT)')
    pieces = re.compile('[\.\-]').split(version)
    snapshot = True if len(pieces) > 3 else False
    prettyprint("Is snapshot release? %s" % snapshot, Levels.DEBUG)
    try:
        for l in f_in:
            if regexp.match(l):
                if l.find('MAJOR') > -1:
                    f_out.write('  val MAJOR = %s\n' % pieces[0])
                elif l.find('MINOR') > -1:
                    f_out.write('  val MINOR = %s\n' % pieces[1])
                elif l.find('MICRO') > -1:
                    f_out.write('  val MICRO = %s\n' % pieces[2])
                #        elif l.find('MODIFIER') > -1:
                #          f_out.write('   val MODIFIER = %s\n' % pieces[3])
                elif l.find('SNAPSHOT') > -1:
                    f_out.write('  val SNAPSHOT = %s\n' % (
                    'true' if snapshot else 'false'))
            else:
                f_out.write(l)
    finally:
        f_in.close()
        f_out.close()

    os.rename(version_java + ".tmp", version_java)

    # Now make sure this goes back into the repository.
    git.commit(modified_files,
               "'Release Script: update versions for %s'" % version)

    # And return the next version
    return pieces[0] + '.' + str(int(pieces[1]) + 1) + '.' + '0' + (
    '-SNAPSHOT' if snapshot else '')


def get_module_name(pom_file):
    tree = ElementTree()
    tree.parse(pom_file)
    return tree.findtext("./{%s}artifactId" % maven_pom_xml_namespace)


def do_task(target, args, async_processes):
    if settings.multi_threaded:
        async_processes.append(Process(target=target, args=args))
    else:
        target(*args)

### This is the starting place for this script.
def release():
    global settings
    global uploader
    global git
    assert_python_minimum_version(2, 5)

    parser = ArgumentParser()
    parser.add_argument('-d', '--dry-run', action='store_true', dest='dry_run',
                        help="release dry run", default=False)
    parser.add_argument('-m', '--multi-threaded', action='store_true',
                        dest='multi_threaded',
                        help="multi threaded release", default=False)
    parser.add_argument('-v', '--verbose', action='store_true', dest='verbose',
                        help="verbose logging", default=True)
    parser.add_argument('-n', '--non-interactive', action='store_true',
                        dest='non_interactive',
                        help="non interactive script", default=False)
    parser.add_argument('-X', '--maven-debug', action='store_true',
                        dest='maven_debug',
                        help="maven debug", default=False)

    # TODO Add branch...
    (settings, extras) = parser.parse_known_args()
    version = extras[0]
    interactive = not settings.non_interactive

    #  require_settings_file()

    #  # We start by determining whether the version passed in is a valid one
    #  if len(sys.argv) < 2:
    #    help_and_exit()

    base_dir = os.getcwd()
    #  version = validate_version(sys.argv[1])
    branch = "master"
    #  if len(sys.argv) > 2:
    #    branch = sys.argv[2]

    prettyprint(
        "Releasing Escalante version %s from branch '%s'" % (version, branch),
        Levels.INFO)

    if interactive:
        sure = input_with_default("Are you sure you want to continue?", "N")
        if not sure.upper().startswith("Y"):
            prettyprint("... User Abort!", Levels.WARNING)
            sys.exit(1)

    prettyprint("OK, releasing! Please stand by ...", Levels.INFO)

    ## Set up network interactive tools
    if settings.dry_run:
        # Use stubs
        prettyprint(
            "*** This is a DRY RUN.  No changes will be committed.  Used to test this release script only. ***"
            , Levels.DEBUG)
        prettyprint("Your settings are %s" % settings, Levels.DEBUG)
        uploader = DryRunUploader()
    else:
        prettyprint("*** LIVE Run ***", Levels.DEBUG)
        prettyprint("Your settings are %s" % settings, Levels.DEBUG)
        uploader = Uploader(settings)

    git = Git(branch, version, settings)
    if interactive and not git.is_upstream_clone():
        proceed = input_with_default(
            'This is not a clone of an %supstream%s Escalante repository! Are you sure you want to proceed?' % (
            Colors.UNDERLINE, Colors.END), 'N')
        if not proceed.upper().startswith('Y'):
            prettyprint("... User Abort!", Levels.WARNING)
            sys.exit(1)

    ## Release order:
    # Step 1: Tag in Git
    prettyprint("Step 1: Tagging %s in git as %s" % (branch, version),
                Levels.INFO)
    tag_release(version, branch)
    prettyprint("Step 1: Complete", Levels.INFO)

    # Step 2: Update version in tagged files
    prettyprint("Step 2: Updating version number in source files", Levels.INFO)
    version_next = update_versions(base_dir, version)
    prettyprint("Step 2: Complete", Levels.INFO)

    # Step 3: Build and test in Maven2
    prettyprint("Step 3: Build and test in Maven2", Levels.INFO)
    maven_build_distribution(settings)
    prettyprint("Step 3: Complete", Levels.INFO)

    async_processes = []

    #  ##Unzip the newly built archive now
    #  unzip_archive(version)
    #
    #  # Step 4: Update javadoc Google Analytics tracker
    #  prettyprint("Step 4: Update Google Analytics tracker", Levels.INFO)
    #  update_javadoc_tracker(base_dir, version)
    #  prettyprint("Step 4: Complete", Levels.INFO)
    #
    #  # Step 5: Upload javadocs to FTP
    #  prettyprint("Step 5: Uploading Javadocs", Levels.INFO)
    #  do_task(upload_javadocs, [base_dir, version], async_processes)
    #  prettyprint("Step 5: Complete", Levels.INFO)
    #
    #  prettyprint("Step 6: Uploading Artifacts", Levels.INFO)
    #  do_task(upload_artifacts, [base_dir, version], async_processes)
    #  prettyprint("Step 6: Complete", Levels.INFO)
    #
    #  prettyprint("Step 7: Uploading to configuration XML schema", Levels.INFO)
    #  do_task(upload_schema, [base_dir, version], async_processes)
    #  prettyprint("Step 7: Complete", Levels.INFO)
    #
    #  prettyprint("Step 8: Uploading to configuration documentation", Levels.INFO)
    #  do_task(upload_configdocs, [base_dir, version], async_processes)
    #  prettyprint("Step 8: Complete", Levels.INFO)

    ## Wait for processes to finish
    for p in async_processes:
        p.start()

    for p in async_processes:
        p.join()

    ## Tag the release
    git.tag_for_release()

    # Update to next version
    prettyprint("Step 9: Updating version number for next release",
                Levels.INFO)
    update_versions(base_dir, version_next)
    prettyprint("Step 9: Complete", Levels.INFO)

    if not settings.dry_run:
        git.push_to_origin()
        git.cleanup()
    else:
        prettyprint(
            "In dry-run mode.  Not pushing tag to remote origin and not removing temp release branch %s." % git.working_branch
            , Levels.DEBUG)

#  prettyprint("\n\n\nDone!  Now all you need to do is the remaining post-release tasks as outlined in https://docspace.corp.redhat.com/docs/DOC-28594", Levels.INFO)

if __name__ == "__main__":
    release()
