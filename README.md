NoteLocation
============

Custom Dialog that returns a new location and an integer that can be used to display the fragment you want.


Installation:
Copy the src/com/braedongeorge/notelocation/NoteLocation.java to your project,
Implement a NoteLocation.NoteLocationListener into your activity and the method onLocationFound.

@Override
	public void onLocationFound(Location location, int typeFrag) {
		if (null == location) {
			Toast.makeText(this, "No Location found", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, "Location: " + String.valueOf(location.getLatitude()) + ", " + String.valueOf(location.getLongitude()), Toast.LENGTH_SHORT).show();
		}
	}


To Display the dialog:

  NoteLocation noteLocation = new NoteLocation(true, 1);
	temp.show(getSupportFragmentManager(), "dialog");
	
	
	
	If you have any difficulties check out the TestApplication.

