#! usr/bin/perl

use strict;
use Fcntl;

my $type;
my $parts;
my $log;

$type = "charcount";
$log = "char";

my $garbage;
sysopen ($garbage, 'char', O_RDWR|O_EXCL|O_CREAT, 0755);
close($garbage);
sysopen ($garbage, 'word', O_RDWR|O_EXCL|O_CREAT, 0755);
close($garbage);

for ($parts = 1; $parts < 100; $parts++) {
    system("java -jar mapreduceFix.jar " . $type . " " . $parts . " " . $log) 
}

$log = "word";
$type = "wordcount";
for ($parts = 11; $parts < 75; $parts++) {
    system("java -jar mapreduceFix.jar " . $type . " " . ($parts * 10) . " " . $log)
}


