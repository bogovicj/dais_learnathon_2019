# dais_learnathon_2019
ImageJ examples for the [2019 DAIS Learnathon](https://imagej.net/2019-06_-_DAIS_learnathon).

## Sample data

Sample data for the [Point rendering example](https://github.com/bogovicj/dais_learnathon_2019/blob/master/src/main/java/net/imglib2/examples/PointRenderingExample.java) can be downloaded [here](https://ndownloader.figshare.com/files/15516080?private_link=3780c3a7f7106d647104).  Extract the contents of the zip file into this repositories `resources` folder.

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
In computer-science language, the point coordinates are represented by
floating-point (`float`/`double`) variables. Continuous transformations
can accept any points as inputs or outputs (on or between the image grid).

The function:

`f(x,y) = ((x + 100), ( y - 0.5 ))`

is a continuous transformation that translates the point `(x,y)` 100 units in the +x
direction, and -0.5 units in the +y direction.

`g(i,j) = ((j), (-i))`

is a discrete transformation that: 
1) Inverts the i axis 
2) Changes the roles of the i- and j-axes.


## Example 1 - Discrete and continuous transforms in imglib2

This example shows how to:
1) Apply a simple translation to an image and display it
2) Apply a complicated discrete transformation to an image and show it
3) Highlights some common pitfalls.
 

## Example 2 - Composing transforms

Composition transformation is just like composing functions - the input of one transformation is
the output of another

This example we will learn:
1) That it is important to interpolate as little as possible.
2) Why that is the case.
3) That transformation composition is the solution.

## Example 3 - Displacement ("deformation") fields
Deformation fields are one of the most common representation of non-linear spatial transformations.

This example we will learn:
1) What a displacement field is.
2) How to construct a deformation field and warp an image with it.
3) How to numerically invert a displacement field.

# Related topics in these examples

* [Imglib2](https://imagej.net/ImgLib2) was the basis of everything here.
* [imglib2-realtransform](https://github.com/imglib/imglib2-realtransform) was the api used for continous transformations
* [Scijava](https://imagej.net/SciJava_Common) was used to 
   * [Display images](https://github.com/bogovicj/dais_learnathon_2019/blob/master/src/main/java/net/imglib2/examples/Ex01_TranformPointsAndImages.java#L63)
   * [Display images with point overlays](https://github.com/bogovicj/dais_learnathon_2019/blob/master/src/main/java/net/imglib2/examples/Helpers.java#L25-L31)
* The [N5](https://github.com/saalfeldlab/n5) api is used to:
   * [store point coordinates in this example](https://github.com/bogovicj/dais_learnathon_2019/blob/master/src/main/java/org/janelia/saalfeldlab/n5examples/PointsToN5.java), and to 
   * [load a displacement field and affine transform in this example](https://github.com/bogovicj/dais_learnathon_2019/blob/master/src/main/java/net/imglib2/examples/PointRenderingExample.java)
