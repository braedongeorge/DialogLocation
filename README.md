DialogLocation
============

Custom Dialog that returns a new location and an integer that can be used to display the fragment you want.


Installation
============

Copy the src/com/braedongeorge/dialoglocation/DialogLocation.java to your project,
Implement a NoteLocation.NoteLocationListener into your activity and the method onLocationFound.
<code>
<pre>
@Override
public void onLocationFound(Location location, int typeFrag) {
	if (null == location) {
		Toast.makeText(this, "No Location found", Toast.LENGTH_SHORT).show();
	} else {
		Toast.makeText(this,
			"Location: " + String.valueOf(location.getLatitude()) + ", " +
			String.valueOf(location.getLongitude()), 	
			Toast.LENGTH_SHORT).show();
	}
}
</pre>
</code>


To display the dialog:
<code>
<pre>
  DialogLocation dialogLocation = new DialogLocation(true, 1);
  temp.show(getSupportFragmentManager(), "dialog");
  </pre>
</code>	
	
Help
============
	
If you have any difficulties check out the TestApplication.

License
============

Copyright 2014 Braedon Reid

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
