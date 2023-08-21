# CatlyNet
<img src="https://github.com/husonlab/catlynet/blob/master/src/main/resources/catlynet/resources/images/splash.png" alt="Splash" width="600"/>

CatlyNet is a program for working with a catalytic reaction system (CRS) consisting of a set of
'catalyzed reactions' and a given 'food set' of molecules.
It provides fast and exact algorithms for calculating the three main types of autocatalytic networks,
namely (maximal) RAFs, CAFs, and pseudo-RAFs. These three notions are formalizations of the concept of a 'collectively
autocatalytic set' originally introduced by Stuart Kauffman. Several other algorithms are also implemented.
The program can visualize a CRS and animate the emergence of a maximal auto-catalytic network.
CatlyNet was first introduced in [[SXH20]](#25).

CatlyNet is written in Java using JavaFX and
is loosely based on a web application written by Dietrich Radel: http://www.math.canterbury.ac.nz/bio/RAF/.

## Installers

One-click installers for MacOS, Windows and Linux are
available [here](https://software-ab.cs.uni-tuebingen.de/download/catlynet/welcome.html).

Versions for iOS and Android are under development.

## Main features

Input to CatlyNet is a catalytic reaction system (CRS), which consists of a list of catalyzed reactions and a food set
of molecules, specified like this example consisting of two food items and three (one-way) reactions:

```
Food: f1 f2

r1 : f2 [f1,p3] => p1
r2 : p1 [f2] => p2
r3 : p2 [p1] => p3
```

Several algorithms are available that aim at computing certain 'auto-catalytic' subsystems.
The three main calculations are the computation of a "maxRAF"
(maximal RAF, reflexively autocatalytic f-generated system), a maxCAF (maximal CAF, constructively autocatalytic and
f-generated system)
and a maxPseudoRAF (maximal pseudo RAF) of a CRS.

The program can display a CRS as a network, in several ways. Moreover, it can animate the emergence of a Max RAF, Max
CAF
or Max Pseudo RAF.

## Algorithms and heuristics

In more detail, the program provides the following calculations:

- Max RAF Algorithm - computes the maximal RAF [[HMS15]](#10) (see also  [[H23]](#40))
- Max CAF Algorithm - computes the maximal CAF [[HMS15]](#10)
- Max Pseudo RAF Algorithm - computes the maximal Pseudo RAF [[HMS15]](#10)
- Strictly Autocatalytic Max RAF Algorithm - computes a Max RAF that has the additional property that any contained
  reaction requires at least one molecule type for catalyzation that is not in the food set [[HXRS23]](#60)
- Min RAF-Generating Given Element Algorithm - Identifies a subset of the Max RAF that is (i) a RAF and (ii) generates a
  given element x
  (not in the food set) and (iii) which is minimal amongst all such sets satisfying (i) and (ii).
- Trivial CAFs Algorithm - computes all reactions that can run using only the food set
- Trivial RAFs Algorithm - computes all reactions that can run using only the food set, where the catalyst need not be
  in the food set if the reaction produces it
- Core RAF Algorithm - computes the unique irreducible RAF, if it exists (Section 4.1 of [[SXH20]](#25))
- Quotient RAF Algorithm - computes the Max RAF minus all the reactions from the Max CAF and adds all produces of the
  Max CAF to the food set [[SXH20]](#25)
- Remove Trivial RAFs Algorithm - computes CRS that is obtained by removing all trivial RAFs
- Min iRAF Heuristic - searches for a smallest irreducible RAF in a heuristic fashion [[HS23]](#50)
- MU CAF Algorithm - computes one maximal uninhibited CAF
- U RAF Algorithm - computes a max RAF, removes inhibited reactions, and then recomputes the max RAF
  (Section 6 of [[HMS16]](#20))
- Run MU CAF Multiple Times... - Runs the MU CAF algorithm multiple times, using different orderings of the input
  reactions
- Determine Necessarily Spontaneous in RAF - determine those reactions that must initially run uncatalyzed and then
  beome catalyzed later
- Greedily Grow MaxCAF to MaxRAF - greedily grow maxCAF to maxRAF by making reactions spontaneous
- Compute Reaction Dependencies - computes the graph of dependencies between all food-set generated
  reactions [[HXRS23]](#60)
- Compute Molecule Dependencies - computes the graph of dependencies between all molecules [[HXRS23]](#60)
- Compute Importance - computes the percent difference between model size and model size without given food
  item [[HS23]](#50)

In addition, the program provides an implementation
of Kauffman's  'binary polymer model'. This features generates a system consisting of all polymers (over a finite
alphabet)
of length at most n, with ligation-cleavage reactions, and with catalysis assigned randomly (each molecule catalysing on
average m reactions).

### Export computed systems

Any of the computed subsystems can be exported to a new file.

## Network visualization

The program can represent a CRS using several types of networks:

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

- Animate Max RAF - in this animation, uncatalyzed reactions run at a low rate, as long as all reactants are present.
- Animate Max CAF - reactions only run (at full rate) when catalyzed and all reactants are present.
- Animate Max Pseudo RAF - same as Max RAF, but additionally, molecules that are not being produced can spontaneously
  arise at a low rate.

Run long enough, in each case, the set of reactions running at full rate, together with the assoicated food sources,
constitutes a subsytem of the animated type.

There are several options:

- Animate Inhibitions - if inhibitions are present in the input CRS, then animate them
- Move Labels - during animation, move copies of the molecule-type labels along the edges rather than just disks
- Use Colors - during animation, give different molecules different colors
- Network Embbedder Iterations - set the number of iterations used by the algorithm that computes the layout of the
  network

<img src="https://github.com/danielhuson/catlynet/blob/master/artwork/animation.gif" alt="Animation" width="600"/>

### Export network

The network can be exported in several image formats, including PNG, GIF, SVG and PDF.

## Commandline tools

The Linux and MacOS distributions have a tools directory that contains two commandline programs:

- catlynet-tool - runs the implemented algorithms on one or more CRS input files
- polymer-tool - generates a set of CRS input files using the binary polymer model

## Provided datasets

The program comes with a number of example datasets (from [here](http://www.math.canterbury.ac.nz/bio/RAF/)):

- example-0.crs - 6 reactions, 3 food items, has a Max RAF of size 3 and no Max CAF
- example-1.crs - 6 reactions and 12 food itens, is a Max RAF and has no Max CAF
- example-2.crs - uses binary polymers, has 5 reactions and 6 food items, has a Max RAF of size 5 and a Max CAF of size
  3
- example-3.crs - 14 reactions and 1 food item, is a Max RAF and has no Max CAF
- example-4.crs - 9 reactions and 1 food item, is a Max RAF and has no Max CAF
- example-5.crs - 15 reactions and 2 food items, is a Max RAF and has no Max CAF
- example-6.crs - 7 reactions and 1 food item, is a Max RAF and has no Max CAF
- example-7.crs - 3 reactions (1 of which is 2-way) and 4 food items, is a Max RAF and a Max CAF
- example-8.crs - uses binary polymers, has 17 two-way reactions and 4 foot items, is a Max RAF and a Max CAF
- example-9.crs - Has a maxRAF of size 4 (which is an iRAF), a pseudoRAF of size 7 (everything) and no CAF (Figure 2
  from [[XHKSM20]](#35))
- example-10.crs - 3 reactions and 1 food item, is a Max RAF and a Max CAF (Fig. 3(c) in [[GFHKU]](#22))
- inhibitions-1.crs - 2 reactions (1 of which has an inhibitor) and 6 food items, Max RAF and Max CAF both have 1
  reaction

## References

<a id="10">[HSM15]</a>
Hordijk, W., Smith, J. I. and Steel, M. (2015). Algorithms for detecting and analysing autocatalytic sets. Algorithms in
Molecular Biology 10: 15.

<a id="20">[HS16]</a>
Hordijk, W. and Steel, M. (2016). Autocatalytic sets in polymer networks with variable catalysis distributions, J. Math.
Chem., 54(10): 1997-2021.

<a id="22">[GFHKU]</a>
Gatti, R.C., Fath, B., Hordijk, W., Kauffman, S. and Ulanowicz, R. (2018).
Niche emergence as an autocatalytic process in the evolution of ecosystems,
Journal of Theoretical Biology, 454: 110-117.

<a id="25">[SXH20]</a>
Steel, M., Xavier, J. C., and Huson, D. H. (
2020). [The structure of autocatalytic networks, with application to early biochemistry.](https://royalsocietypublishing.org/doi/10.1098/rsif.2020.0488)
J. Royal Society Interface, 17: 20200488.

<a id="35">[XHKSM20]</a>
Xavier, J. C., Hordijk, W., Kauffman, S., Steel M. and Martin, W. F. (2020). Autocatalytic chemical networks at the
origin of metabolism. Proc. Roy. Soc. B. 287: 20192377

<a id="40">[H23]</a>
Hordijk, W. (2023). A concise and formal definition of RAF sets and the RAF algorithm, arXiv:2303.01809.

<a id="50">[HS23]</a>
Huson, D. and Steel, M. (2023) CatlyNet - Implemented Algorithms (unpublished).

<a id="60">[HXRS23]</a>
Huson, D., Xavier, J., Rodrigo, A. and Steel, M. (2023). Structural and algorithmic results for self-generating
autocatalytic networks. (In prep).