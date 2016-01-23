#!/usr/bin/perl

use strict;
use warnings;

use YAML::XS 'LoadFile','Dump';

my $input = LoadFile($ARGV[0]);

# Exit early if this is not a map config
if(!exists($input->{maps})) {
    exit 0;
}

my $maps = $input->{maps};

my @mapNames = keys %$maps;

my $mapName = $mapNames[0];

my $mapConfig = $maps->{$mapName};

print "folder: \"mccw_$mapConfig->{folder}\"\n";
print "description: \"Map description here\"\n";

if(exists $mapConfig->{environment}) {
    print "environment: $mapConfig->{environment}\n";
} else {
    print "environment: normal\n";
}

print "enable_weather: false\n\n";
print "shared_objects:\n";
print "  showdown:\n";
print "    center:\n";

foreach my $axis ('x', 'y', 'z') {
    print "      $axis: $mapConfig->{center}->{$axis}\n";
}

print "    size:\n";

foreach my $axis ('x', 'z') {
    print "      $axis: $mapConfig->{'showdown-size'}->{$axis}\n";
}

print "  spawn_locations:\n";

foreach my $team (keys %$mapConfig) {
    if($team eq 'folder' || $team eq 'center' || $team eq 'showdown-size' || $team eq 'environment') {
        next;
    }
    print "    $team:\n";

    my $teamConfig = $mapConfig->{$team};

    foreach my $axis ('x', 'y', 'z') {
        my $value = $teamConfig->{spawn}->{$axis};
        if($axis eq 'y') {
            $value++;
        }
        print "      $axis: $value\n";
    }
}

print "\nlisteners:\n";
print "  initial_buildings:\n";
print "    class: \"com.ithinkrok.cw.map.InitialBuildingSpawner\"\n";
print "    config:\n";
print "      initial_buildings:\n";

foreach my $team (keys %$mapConfig) {
    if($team eq 'folder' || $team eq 'center' || $team eq 'showdown-size' || $team eq 'environment') {
        next;
    }

    print "        - building: \"Base\"\n";
    print "          team: \"$team\"\n";
    print "          location:\n";

    my $teamConfig = $mapConfig->{$team};

    foreach my $axis('x', 'y', 'z') {
        print "            $axis: $teamConfig->{base}->{$axis}\n";
    }
}