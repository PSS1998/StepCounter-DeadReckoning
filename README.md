# StepCounter-DeadReckoning
This is an Android application that solves Dead reckoning with help of step counter.<br/>
<br/>
## Modules
StepCounterService: This module detects each step with the use of accelerator sensor.<br/>
RoutingService: This module with each step and the direction of heading for that step tries to map the users movement.<br/>
ActivityRecognition: With ActivityRecognitionAPI of Google Play Service tries to classify the type of activity.<br/>
InPocketDetection: Determines wether the phone is in user's hand or pocket.<br/>
GyroOrientation: Calculate heading with the use of gyroscope sensor.<br/>
Orientation: Compass with magnetic declination fixed.<br/>
ScatterPlot: Plot the movement of user.<br/>
Filter: Implementation of Low Pass Filter, Complementary Filter and Median Filter for increase of accuracy.<br/>
<br/>
## ScreenShot of App
This is an example of movement of about 700 meters with this app whithout the use of GPS.<br/>
ScreenShot:<br/>
![ScreenShot](https://github.com/PSS1998/StepCounter-DeadReckoning/blob/master/Reports/ScreenShot.png?raw=true)
Movement on Map with same scale:<br/>
![Map](https://github.com/PSS1998/StepCounter-DeadReckoning/blob/master/Reports/map.png?raw=true)
<br/>
## TODO
- [ ] Add movement calculation for other than walking.
- [ ] Add heading detection when phone not in hand.
