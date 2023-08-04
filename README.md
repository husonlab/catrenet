# CatlyNet
<img src="https://github.com/danielhuson/catlynet/blob/master/src/catlynet/resources/images/splash.png" alt="Splash" width="600"/>

CatlyNet is a program for working with autocatalytic networks. It is written in Java and uses JavaFX. The program
implements a fast and exact algorithm for calculating (maximal) RAFs, CAFs, and pseudo-RAFs from any set of catalysed '
reactions' and a given 'food' set. These three notions are formalizations of the concept of a 'collectively
autocatalytic set' originally introduced by Stuart Kauffman.

CatlyNet is loosely based on a web application written by Dietrich Radel: http://www.math.canterbury.ac.nz/bio/RAF/

## Further reading

Here is a publication that describes and uses CatlyNet:

Steel M, Xavier JC, Huson DH. 2020 The structure of autocatalytic networks, with application to early biochemistry. J. R. Soc. Interface 17: 20200488. http://dx.doi.org/10.1098/rsif.2020.0488

Read more about autocatlyic networks and RAFs here:

Hordijk, W. and Steel, M. (2017)
. [Chasing the tail: The emergence of autocatalytic networks. Biosystems](http://www.sciencedirect.com/science/article/pii/S030326471630274X)
, 152: 1-10.

## Installers

One click installers for MacOS, Windows and Linux are
available [here](https://software-ab.cs.uni-tuebingen.de/download/catlynet/welcome.html).

## Main features

Input to CatlyNet is a food set and a list of reactions, a catalytic reaction system (CRS).

Several algorithms are available that aim at computing certain subsystems. The three main calculations are the
computation of the "maxRAF"
(maximal RAF, reflexively autocatalytic f-generated system), the maxCAF (maximal CAF, constructively autocatalytic and
f-generated system)
and the maxPseudoRAF (maximal pseudo RAF) of a CRS.

The program can display a CRS as a network, in several different ways. Moreover, it can animate the emergence of a
maxRAF, maxCAF or maxPseudoRAF.

## Algorithms and heuristics

The program provides the following calculations:

- Max RAF Algorithm - compute the maximal RAF
- Max CAF Algorithm - compute the maximal CAF
- Max Pseudo RAF Algorithm - compute the maximal Pseudo RAF
- Strictly Autocatalytic Max RAF Algorithm - compute a Max RAF that has the additional property that any contained
  reaction requires at least one molecule type for catalyzation that is not in the food set
- Trivial CAFs Algorithm
- Trivial RAFs Algorithm
- Core RAF Algorithm
- Quotient RAF Algorithm
- Remove Trivial RAFs Algorithm
- Min Irr RAF Heuristic
- MU CAF Algorithm
- U RAF Algorithm
- Run MU CAF Multiple Times...
- Determine Necessarily Spontaneous in RAF
- Greedily Grow MaxCAF to MaxRAF
- Compute Reaction Dependencies
- Compute Molecule Dependencies
- Compute Importance
-

### Export computed systems

Any of the computed subsystems can be exported to a new file.

## Network visualization

The program can represent a CRS in using several different types of networks:

- Full network - this shows all reactions, food items, reactants, produces and catalysts as nodes connected by directed
  edges.
- Association Network - reactions are shown as nodes and a directed edge from one reaction to the other indicates that
  the one reaction produces a reactant or catalyst for the other
- Reactant Association Network - reactions are shown as nodes and a directed edge indicates that one reaction produces a
  reactant for the other.
- Reaction-Dependency Network - shows dependencies between reactions (this feature requires testing and debugging)
- Molecule-Dependency Network - show dependencies between molecules (this feature requires testing and debugging)

There are a several options:

- Suppress Formal Food Item - if there is a formal food item present, which is used to implement "catalyst-free"
  reactions, then selecting this option will hide it
- Suppress Catalyst Edges - select this to hide catalyst edges
- Use Multicopy Food Nodes - select this to show each food node multiple times, to produce a less tangled network
- Show/Hide Node Labels - select to hide node labels

### Animation

The program can animate the emergence of three types of systems:

- Animate Max RAF - in this animation, uncatalyzed reactions proceed at a low rate
- Animate Max CAF - reactions only proceed when catalyzed
- Animate Max Pseudo RAF - reactions proceed at a low rate, when uncatalyzed or even missing a reactant

There are several options:

- Animate Inhibitions - if inhibitions are present in the input CRS, then animate them
- Move Labels - during animation, move copies of the molecule-type labels along the edges rather than just disks
- Use Colors - during animation, give different molecules different colors
- Network Embbedder Iterations - set the number of iterations used by the algorithm that computes the layout of the
  network

<img src="https://github.com/danielhuson/catlynet/blob/master/artwork/animation.gif" alt="Animation" width="600"/>

### Export network

The network can be exported in several image formats, including PNG, GIF, SVG and PDF.