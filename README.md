# Autonomous-Cartographer-Robot

This was the main project of my master degree's first year. It lasted from september 2015 to June 2016. We were three people on it.
The robot is a Lego mindstorm embedded system, and we used the Lejos Java API to program this robot.
The purpose of this project was to go as far as we could on the developing of this project. Unfortunately, the teachers prefered to evaluate
us on how we work in team and make us program as much as we can, than asking for a final operationnal product. Thus, the final purpose of this robot
have not been reached, but it was close. Let's give some more details about it.

The purpose of this robot is to explore an enclosed area, and retrieve the data to a computer via the network. The computer then retrives
a map of the discovered area. The robot was provided with a sonar sensor, which retrives the distance from an obstacle. 

At the end of the first semester, the robot was capable of making a slow 360 degrees turn around itself, and transmitting to our computer the data so as 
to display every points discoverd on the computer. Here is the video : https://www.youtube.com/watch?v=B_eD46GiDtA

On the second semester, our goal was to interpret those points and make the robot take decisions, make the robot find itself the place he has to go to.
The smartest way of doing it was to create a simulator. We programmed a user interface, in which we can create some points by clicking inside the main area,
to simulate what the robot would probably give us. We then worked on how to interpret these points. 
First of all, we used union-find algorithm, to gather the closest points between them, and retrieve connected component.
Then, what we did was setting the "currently discovered zone" and the most likely ways out. Then we were able to construct a graph, by setting vertices
on every most likely way out and connect them to the vertice that represents the current robot position.

Then we simulated the robot going to one of this points and programmed the fusion of two discovered zone. If two connected components are close, 
they are gathered and represent one and only connected component. If the two extremities of a connected component reach out, we know that we detected an object.

At the end of the project, we did not had much time remaining and we did our best to update the simulator on the robot. 
We did, but it was not as efficient as we wanted. 

The only part of the application you test is the simulator.
