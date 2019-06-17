# dais_learnathon_2019
ImageJ examples for the [2019 DAIS Learnathon](https://imagej.net/2019-06_-_DAIS_learnathon).

# Overview

These examples show how to apply spatial transformations 
to points and images using imglib2.

## Spatial transformations

Spatial transformations change an image by "moving the image" rather
than affecting pixel values.  A transform is a function with a point
coordinate as an input, and another point coordinate as an output.

### Discrete transformations

Discrete transformations have discrete valued inputs and outputs. 
In compute-science terms, this means that the point coordinates 
are stored as integer (`int`/`long`) valued variables.
We will call this set of discrete location the "image grid".
Discrete transforms can not accept points off the image grid, 
nor can they produce 

### Continuous ("real") transformations

Continuous transformations have continous valued inputs and outputs. 
In math language, the point coordinates are Real numbers.  
In computer-science language, the point coordinates are
represented by floating-point (`float`/`double`) variables.
Continuous transformations can accept any points as inputs or outputs
(on or between the image grid).

The function:

`f(x,y) = ((x + 100), ( y - 0.5 ))`

is a continuous transformation that translates the point `(x,y)` 100 units in the +x
direction, and -0.5 units in the +y direction.

`g(i,j) = ((j), (-i))`

is a discrete transformation that: 
1) Inverts the i axis 
2) Changes the roles of the i- and j-axes.


# Example 1 - Discrete transforms in imglib2

This example shows how to:
1) Apply a simple translation to an image and display it
2) Apply a complicated discrete transformation to an image and show it
3) Highlights some common pitfalls.
 
