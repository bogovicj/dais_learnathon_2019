# transforms_tutorial
Imglib2 / ImageJ examples for the [2019 DAIS Learnathon](https://imagej.net/2019-06_-_DAIS_learnathon).

## Sample data

Sample data for the [Point rendering example](https://github.com/bogovicj/transforms_tutorial/blob/master/src/main/java/net/imglib2/examples/PointRenderingExample.java) can be downloaded [here](https://ndownloader.figshare.com/files/15516080?private_link=3780c3a7f7106d647104).  Extract the contents of the zip file into this repositories `resources` folder.

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

Exercise:
Use what we learned in these examples to resample an image (preserving the field of view).

## Example 2 - Composing transforms

Composition transformation is just like composing functions - the input of one transformation is
the output of another.

See [this Robert Haase tweet](https://twitter.com/haesleinhuepf/status/1088546103866388481) and the related discussion.

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
4) Adding deformation fields is not the same as composing them.

## Point rendering example

This example reiterates some of the topics we've learned about in other examples with a more realistic example.
Before starting, make sure you've downloaded [this sample data](https://ndownloader.figshare.com/files/15516080?private_link=3780c3a7f7106d647104) 
and extracted the contents of the zip file into this repositories `resources` folder.

The light image data comes from [An unbiased template of the Drosophila brain and ventral nerve cord](https://doi.org/10.1101/376384).  The EM image data come from the [Complete Electron Microscopy Volume of the Brainof Adult Drosophila melanogaster ("FAFB")](https://doi.org/10.1016/j.cell.2018.06.019) image data [available here](https://www.temca2data.org/), and the synapse predictions from those data are from [Synaptic Cleft Segmentation in Non-Isotropic Volume Electron Microscopy of the Complete Drosophila Brain](https://arxiv.org/abs/1805.02718)


# Tips for a joyful transformation experience

* **Save your transforms to disk** when transforming images.
   * It's *more important* to save the transforms, than to save the images(!)
* Use and respect the image metadata
    * Set pixel spacing and origin in physical units
* Define transforms in physical units (nm, um, mm, au, whatever) not pixels.
   * This makes your life super easy when dealing with images of the same thing taken at different resolutions
   and/or with slightly different fields of view.  
* Respect your origin
   * Pick it, and stick with it.
   * Don't define rotations about the center because it's convenient (now), it will cause pain (later).


# Related topics in these examples

* [Imglib2](https://imagej.net/ImgLib2) is the basis of everything here.
* [imglib2-realtransform](https://github.com/imglib/imglib2-realtransform) is the api used for continous transformations
* [Scijava](https://imagej.net/SciJava_Common) is used to 
   * [Display images](https://github.com/bogovicj/transforms_tutorial/blob/master/src/main/java/net/imglib2/examples/Ex01_TranformPointsAndImages.java#L63)
   * [Display images with point overlays](https://github.com/bogovicj/transforms_tutorial/blob/master/src/main/java/net/imglib2/examples/Helpers.java#L25-L31)
* [BigDataViewer](https://imagej.net/BigDataViewer) is used for visualization in the [PointsRenderingExample](https://github.com/bogovicj/transforms_tutorial/blob/1d7c929b988d4f61a7d9e80040cd543397f3db36/src/main/java/net/imglib2/examples/PointRenderingExample.java#L124)
* The [N5](https://github.com/saalfeldlab/n5) api is used to:
   * [store point coordinates in this example](https://github.com/bogovicj/transforms_tutorial/blob/master/src/main/java/org/janelia/saalfeldlab/n5examples/PointsToN5.java), and to 
   * [load a displacement field and affine transform in this example](https://github.com/bogovicj/transforms_tutorial/blob/master/src/main/java/net/imglib2/examples/PointRenderingExample.java)
