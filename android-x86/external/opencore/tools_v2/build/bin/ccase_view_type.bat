@rem = 'Perl, ccperl read this as an array assignment & skip the goto
@echo off
goto endofperl
@rem ';
#!/usr/atria/bin/Perl

######################################################################

my $val = `cleartool lsview -cview -prop -full 2>&1`;

if ($val =~ m/Properties:(.*)\s+dynamic\s(.*)/) {
    print "dynamic\n";
    exit(0);
} else {
    print "Unknown\n";
    exit(-1);
}
exit(0);

__END__

:endofperl
ccperl -e "$s = shift; $c = $s =~ /.bat$/ ? $s : $s.'.bat'; $p = (-x $c) ? '' :' S '; system('ccperl '.$p.$c.' '.join(' ',@ARGV)); exit $?;" %0 %1 %2 %3 %4 %5 %6 %7 %8 %9
