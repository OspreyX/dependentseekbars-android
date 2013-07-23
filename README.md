#DependentSeekBars for Android
***

DependentSeekBars is a library that allows Android SeekBars to have relationships amongst each other. For example, you can have one SeekBar's progress always be less than another's. DependentSeekBars aims to take care of this restriction logic for you making it easier.

A DependentSeekBar is a superset of a SeekBar, so you would use a DependentSeekBar the same exact way you would use a SeekBar.

Usage
-----

*There are example activities in example/ that showcase the usage*

1. Create a DependentSeekBarManager which will keep track of DependentSeekBars

        DependentSeekBarManager manager = new DependentSeekBarManager();

2. Create a DependentSeekBar and add it to the DependentSeekBarManager
    
        DependentSeekBar seekBar1 = manager.createSeekBar(context, progress);


    A DependentSeekBar is a view so alternatively you can have it in a layout XML
    
        <com.oanda.dependentseekbars.DependentSeekBar
            android:id="@+id/seekbar2"
            android:layout_width="fill_parent"
            android:layout_height="wrap_content" />
    
    And add it to the DependentSeekBarManager like this
    
        setContentView(R.layout.seekbar_layout);
        DependentSeekBar seekBar2 = (DependentSeekBar) findViewById(R.id.seekBar2);
        manager.addSeekBar(seekBar2);

3. Let's add a dependency on seekBar2 so that its progress is always greater than seekBar1's

        seekBar2.addDependency(DependentSeekBar.Dependency.GREATER_THAN, seekBar1);
        

Features
--------

<b>Progress Shifting</b>

When a DependentSeekBar's progress is bounded by another, the DependentSeekBarManager will attempt to move the restricting DependentSeekBar's progress. If you are moving a DependentSeekBar's progress and you come across a bounding DependentSeekBar, this feature will push the restricting DependentSeekBar's progress until the maximum or minimum progress.  This can be turned off via the DependentSeekBarManager by calling setShiftingAllowed(false).

License
-------

   Copyright 2013 OANDA Corporation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
