###########################################
#
# RulesPassesTest module
# - module to test predefined passes
#
###########################################

package RulesPassesTest;

use File::Path;
use File::Basename;
use OdeEnv;
use OdeFile;
use OdeTest;
use OdePath;
use OdeUtil;
use OdeInterface;
use strict;
use Cwd;

use RulesPassesTest_standard;
use RulesPassesTest_explib;
use RulesPassesTest_expinc;
use RulesPassesTest_objects;
use RulesPassesTest_setup;
use RulesPassesTest_javadoc;
#########################################
#
# sub run
#
#########################################
sub run ()
{
  my $status;
  my $error = 0;
  my $rules_error = 0;
  my $command;
  my $error_txt;

  OdeInterface::logPrint( "in RulesPassesTest\n");

  ## Testing predefined passes
  ## testing STANDARD pass
  $status = RulesPassesTest_standard::run();
  if (!$error) { $error = $status; }

  ## testing EXPLIB pass
  $status = RulesPassesTest_explib::run();
  if (!$error) { $error = $status; }

  ## testing EXPINC pass
  $status = RulesPassesTest_expinc::run();
  if (!$error) { $error = $status; }

  ## testing OBJECTS pass
  $status = RulesPassesTest_objects::run();
  if (!$error) { $error = $status; }

  ## testing SETUP pass
  $status = RulesPassesTest_setup::run();
  if (!$error) { $error = $status; }

  ## testing JAVADOC pass
  $status = RulesPassesTest_javadoc::run();
  if (!$error) { $error = $status; }

  return $error;
}

1;
