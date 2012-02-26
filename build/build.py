import os
import sys
import zipfile
import glob
import stat

jboss_version = os.environ['JBOSSVERSION']
base_dir = os.environ['BASEDIR']
target_dir = "%s/target" % base_dir
jboss_prefix = "jboss-as"
target_jboss = "%s/%s" % (target_dir, jboss_prefix)
m2_repo = "%s/.m2/repository" % os.getenv("HOME")
jboss_zip = "%s/org/jboss/as/jboss-as-dist/%s/jboss-as-dist-%s.zip" % \
            (m2_repo, jboss_version, jboss_version)

def unzip_as(jboss_zip, target_dir):
  print "Unzip JBoss AS distribution to %s" % target_dir
  os.mkdir(target_dir, 0777)
  zip_file = zipfile.ZipFile(jboss_zip)
  for name in zip_file.namelist():
    if name.endswith('/'):
      os.mkdir(os.path.join(target_dir, name))
    else:
      out_file = open(os.path.join(target_dir, name), 'wb')
      out_file.write(zip_file.read(name))
      out_file.close()
  # Rename to generic jboss app server directory
  unzipped_as_dir = "%s/%s-%s" % (target_dir, jboss_prefix, jboss_version)
  os.rename(unzipped_as_dir, target_jboss)

def make_sh_files_exec(target_jboss):
  executables = glob.glob("%s/bin/*.sh" % target_jboss)
  for e in executables: os.chmod(e, stat.S_IRWXU)

def main():
  print """
   |--------------------------------------
   | Explode base JBoss Application Server
   |--------------------------------------
   | base dir = %s
   | jboss version = %s
   |--------------------------------------
   """ % (base_dir, jboss_version)

  if os.path.exists(target_jboss):
    print "JBoss AS distribution already extracted"
  else:
    unzip_as(jboss_zip, target_dir)
    make_sh_files_exec(target_jboss)

  # Add gap
  print ""

if __name__ == "__main__": main()

