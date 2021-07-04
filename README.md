# SkyNeuralNet Usage Example
This is an example project which showcases how a fully connected feedforward neural network, created by [SkyNeuralNet](https://github.com/SiTronXD/SkyNeuralNet), can be used in an arbitrary application. It works on both desktops and android phones, as it was written with Java and LibGDX.

The interface is quite simple. The user can draw a number 0-9 inside the black square, and the network will take a guess at what number it is looking at. 

The network itself works fairly well, often times being able to see the numbers 0-4, usually 5, but can have trouble seeing the numbers 6-9. Since it is a simple network, the position and size of the number matters a great deal. As expected, the success rate increases if the number is drawn in a similar style as the training data, which originally comes from the [MNIST database of handwritten digits](http://yann.lecun.com/exdb/mnist/).

# Screenshots
<p align="center">
  <img width="250" height="528" src="https://github.com/SiTronXD/SkyNeuralNetUsageExample/blob/main/SkyNeuralNetUsage/android/assets/Showcase1.png">
  <img width="250" height="528" src="https://github.com/SiTronXD/SkyNeuralNetUsageExample/blob/main/SkyNeuralNetUsage/android/assets/Showcase2.png">
  <img width="250" height="528" src="https://github.com/SiTronXD/SkyNeuralNetUsageExample/blob/main/SkyNeuralNetUsage/android/assets/Showcase3.png">
</p>
