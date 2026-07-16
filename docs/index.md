---
title: CatReNet User Manual
layout: default
---

# CatReNet User Manual

**CatReNet** (formerly *CatlyNet*) is an interactive program for exploring **(auto‑)catalytic reaction networks**. Given
a *food set* of molecules and a list of *catalyzed reactions*, it uses fast, exact algorithms to compute self‑sustaining
subnetworks — RAFs, CAFs, and pseudo‑RAFs — and can visualize a network and animate the emergence of such a subnetwork.

CatReNet is an open‑source Java/JavaFX application that runs on Linux, macOS, and Windows. It was developed by Daniel H.
Huson, Joana C. Xavier, and Mike A. Steel, and is distributed under the GPL‑3 license.

- Project page and source: <https://github.com/husonlab/catrenet>
- Video introduction: <https://youtu.be/8sBZtX101gU>

This is a concise getting‑started guide for researchers. It covers installation, the input file format, running the core
computations, and viewing, animating, and exporting results.

---

## 1. Key concepts

A **catalytic reaction system (CRS)** is the input to CatReNet. It consists of:

- a **food set** — molecule types assumed to be freely available, and
- a set of **reactions**, each transforming reactants into products, usually enabled (catalyzed) by one or more catalyst
  molecules.

Within a CRS, CatReNet looks for *self‑sustaining* subnetworks — formalizations of Stuart Kauffman's notion of a "
collectively autocatalytic set." The three main types are:

- **CAF** (constructively autocatalytic and food‑generated) — every reaction can proceed only in the presence of a
  catalyst, and all reactants and catalysts trace back to the food set. This is the most restrictive notion.
- **RAF** (reflexively autocatalytic and food‑generated) — reactions may initially run at a low, uncatalyzed rate once
  their reactants are present, as long as each reaction is eventually catalyzed by a product of the network. Every CAF
  is a RAF.
- **pseudo‑RAF** — like a RAF, but molecules that are not yet being produced may also arise spontaneously at a low rate.
  This is the least restrictive notion.

CatReNet computes the **maximal** subnetwork of each type (the maxRAF, maxCAF, and max pseudo‑RAF), and provides many
further algorithms — for example finding a minimal RAF that produces a target molecule, or a unique irreducible "core"
RAF.

---

## 2. Installation

Interactive installers for **macOS, Windows, and Linux** are available from the download page linked from
the [GitHub repository](https://github.com/husonlab/catrenet). Download the installer for your platform and follow the
prompts. (Android and iOS versions are under development.)

The installer places a folder of example datasets, named **`CatReNet-Examples`**, in your installation directory. These
are useful for trying out the program immediately.

---

## 3. Getting started

1. Launch CatReNet.
2. Choose **File → Open**, navigate to the `CatReNet-Examples` folder, and open an example such as `example-0.crs`.
3. The dataset opens in its own window with an **Input food** tab, an **Input reactions** tab, and a **Log** tab that
   records the results of any computations you run.
4. Run a computation from the toolbar or menu — for example **Max RAF** — and read the result in the Log tab.
5. Switch to the **Network** view to see the reaction system drawn as a graph, or animate the emergence of a
   maxRAF/maxCAF/max pseudo‑RAF.

Each open dataset lives in its own window, so you can work with several systems at once.

---

## 4. Input format (CRS files)

CatReNet reads plain‑text **`.crs`** files. A file lists a food set and one reaction per line. You can type a system
directly into the input tabs, or prepare a file in any text editor.

A minimal example with two food items and three one‑way reactions:

```
Food: f1 f2

r1 : f2 [f1,p3] => p1
r2 : p1 [f2] => p2
r3 : p2 [p1] => p3
```

### Food set

Begin a line with `Food:` (or `Foods:`) followed by the food molecules, separated by spaces or commas:

```
Food: a, b, c
```

### Reactions

Each reaction has the form:

```
name : reactants [catalysts] {inhibitors} -> products
```

- **name** — a label for the reaction, followed by a colon (e.g. `r1:`).
- **reactants** and **products** — molecule types separated by `+` or spaces. Stoichiometric coefficients may be given (
  e.g. `2 a + b`).
- **catalysts** — listed in **square brackets** `[ ]`. The `{inhibitors}` part is optional.
- **inhibitors** — listed in **curly braces** `{ }`; a reaction cannot proceed while an inhibitor is present.

### Arrows (reaction direction)

- `->` or `=>` — one‑way (forward) reaction
- `<-` or `<=` — reverse reaction
- `<->` or `<=>` — two‑way (reversible) reaction

### Catalysis rules (AND / OR)

Catalysts inside the brackets can express Boolean conditions:

- Separate alternatives with a **comma**, a **space**, or `|` to mean **OR** (any one of them catalyzes the reaction).
  Example: `[a, b]` means *a* **or** *b*.
- Join required catalysts with `&` (or `*`) to mean **AND** (all are required together). Example: `[a & b]` means *a* *
  *and** *b*.

These can be combined, e.g. `[c, d, e&f]` means *c*, or *d*, or (*e* and *f* together).

### Comments

Lines beginning with `#` are comments and are ignored.

### Worked examples

A small system with a two‑way reaction and combined catalysis (`example-7.crs`):

```
# maxCAF all, maxRAF all, max-pRAF all
r1: a+b [c,d,e*f] -> d
r2: c+d [a] -> a
r3: c+a [a] <-> b

Food: a b c d
```

An intuitive "how to build a house" system (`how-to-build-a-house-maxRAF.crs`):

```
Food: wood, metal, nails, stone

R0 : stone [hammer] -> sharp_stone
R1 : wood [sharp_stone] -> handle
R2 : handle + metal [sharp_stone] -> hammer
R3 : handle + metal [hammer] -> saw
R4 : wood [saw] -> planks
R5 : planks [hammer&saw] -> panel
R6 : nails + panel [hammer] -> house
```

A system with an inhibitor (`inhibitions-1.crs`):

```
Food: a, b, c, d, x, z

r1 : a + b [x] => y
r2 : c + d [z] {y} => x + z
```

---

## 5. Running computations

Select an algorithm from the toolbar or menus. Results are written to the **Log** tab, and any computed subsystem can be
exported to a new file (see §8). The main computations are:

- **Max RAF** — the maximal reflexively autocatalytic f‑generated system.
- **Max CAF** — the maximal constructively autocatalytic f‑generated system.
- **Max Pseudo RAF** — the maximal pseudo‑RAF.

Additional algorithms and heuristics include:

- **Strictly Autocatalytic Max RAF** — a maxRAF in which every reaction requires at least one catalyst that is *not* in
  the food set.
- **Min RAF‑Generating Given Element** — the smallest RAF within the maxRAF that generates a chosen molecule (not in the
  food set). Useful for asking "what minimal network is needed to produce X?"
- **Core RAF** — the unique irreducible RAF, if one exists.
- **Quotient RAF** — the maxRAF with the maxCAF reactions removed and the maxCAF products added to the food set.
- **Min iRAF Heuristic** — a heuristic search for a smallest irreducible RAF (you can set the number of randomized
  runs).
- **Trivial CAFs / Trivial RAFs** — reactions that can already run using only the food set.
- **Remove Trivial RAFs** — the CRS with all trivial RAFs removed.
- **MU CAF** — one maximal uninhibited CAF; **Run MU CAF Multiple Times…** repeats this over different reaction
  orderings.
- **U RAF** — computes a maxRAF, removes inhibited reactions, then recomputes the maxRAF.
- **Determine Necessarily Spontaneous in RAF** — reactions that must first run uncatalyzed and become catalyzed later.
- **Greedily Grow MaxCAF to MaxRAF** — grow the maxCAF into the maxRAF by making reactions spontaneous.
- **Compute Reaction Dependencies / Molecule Dependencies** — dependency graphs among reactions or molecules.
- **Stratify Reactions and Molecules** — ranks reactions and molecules by the order in which they can first appear.
- **Compute Importance** — the percentage change in model size when a given food item is removed.

CatReNet also implements **Kauffman's binary polymer model**, which generates a system of all polymers over a finite
alphabet up to length *n*, with ligation/cleavage reactions and randomly assigned catalysis (each molecule catalyzing
*m* reactions on average).

---

## 6. Visualizing networks

Switch to the **Network** view to draw the reaction system. Because layout of very large graphs is demanding, network
menu items are **disabled for systems with more than 500 reactions** by default; you can raise this threshold in the
Settings panel.

Available network types:

- **Full network** — shows reactions, food items, reactants, products, and catalysts as nodes joined by directed edges.
- **Association network** — reactions are nodes; an edge from one reaction to another means the first produces a
  reactant *or* catalyst for the second.
- **Reactant association network** — as above, but only for produced *reactants*.
- **Reaction‑dependency** and **Molecule‑dependency networks** — dependency views (still experimental).

Display options include: suppress the formal food item used for catalyst‑free reactions, suppress catalyst edges, use
multi‑copy food nodes for a less tangled layout, and show or hide node labels.

> **Tip:** the built‑in layout is intentionally basic. For publication‑quality figures, export the network in **GML**
> format and open it in [Cytoscape](https://cytoscape.org).

---

## 7. Animation

CatReNet can animate how a self‑sustaining network emerges over time:

- **Animate Max RAF** — uncatalyzed reactions run at a low rate whenever all their reactants are present.
- **Animate Max CAF** — reactions run only when catalyzed and all reactants are present.
- **Animate Max Pseudo RAF** — as Max RAF, but unproduced molecules may also arise spontaneously at a low rate.

Run long enough, the reactions running at full rate (with their food sources) form a subsystem of the animated type.
Options let you animate inhibitions, move molecule labels along edges, use colors to distinguish molecule types, and set
the number of network‑embedding iterations.

---

## 8. Exporting results

- **Computed subsystems** — any computed RAF/CAF/etc. can be exported to a new `.crs` file for further analysis.
- **Network images** — export the current network as **PNG**, **SVG**, or **PDF**.
- **GML for Cytoscape** — use **File → Export → Network in GML Format…**, then import the file into Cytoscape via its
  *File → Import → Network From File* menu.

---

## 9. Settings

The Settings panel configures:

- **Reaction notation** — full, sparse, or tabbed; and the arrow style (`->` vs `=>`).
- **Default node/edge styles** — line style, color, and width of edges; shape, color, and size of nodes.
- **Display labels** — a lookup table mapping identifiers to readable names (e.g. the KEGG code `C00005` → *reduced
  nicotinamide adenine dinucleotide phosphate*).
- **Animation** — colors per molecule and whether labels are used.
- **Other** — maximum reactions allowed for drawing, number of embedding iterations, and text wrapping.

---

## 10. Command‑line tools

The Linux and macOS distributions include a `tools` directory with two command‑line programs for batch or scripted
analysis:

- **`catrenet-tool`** — runs the implemented algorithms on one or more CRS input files.
- **`polymer-tool`** — generates CRS input files using the binary polymer model.

Common `catrenet-tool` options:

| Option | Long form            | Meaning                                                                |
|--------|----------------------|------------------------------------------------------------------------|
| `-c`   | `--compute`          | The computation to perform (e.g. the Max RAF algorithm) — **required** |
| `-i`   | `--input`            | Input file (`stdin` accepted) — **required**                           |
| `-o`   | `--output`           | Output file (defaults to `stdout`)                                     |
| `-rn`  | `--reactionNotation` | Output reaction notation (full, sparse, tabbed)                        |
| `-an`  | `--arrowNotation`    | Output arrow notation                                                  |
| `-r`   | `--runs`             | Number of randomized runs (for the Min iRAF heuristic)                 |
| `-h`   | `--help`             | Show all options                                                       |

Run either tool with `-h` to see the full, up‑to‑date list of options and the available computation names.

---

## 11. Example datasets

The bundled `CatReNet-Examples` folder includes systems of increasing size and complexity, for example:

- **`example-0.crs`** — 6 reactions, 3 food items; has a maxRAF of size 3 and no maxCAF.
- **`example-7.crs`** — 3 reactions (one two‑way), 4 food items; is both a maxRAF and a maxCAF, and requires combined ("
  and") catalysis.
- **`example-9.crs`** — maxRAF of size 4 (an irreducible RAF), a pseudo‑RAF containing everything, and no CAF.
- **`inhibitions-1.crs`** — 2 reactions (one inhibited), 6 food items.
- **`how-to-build-a-house-maxRAF.crs`** — an intuitive 7‑reaction example with no maxCAF.
- **`prokaryotic-network.crs`** — a real prokaryotic catalytic reaction network with **6039 reactions** and **68 food
  items**.

---

## 12. Citing CatReNet

If you use CatReNet in your research, please cite:

> Huson, D. H., Xavier, J. C., & Steel, M. A. (2024). *CatReNet: interactive analysis of (auto‑)catalytic reaction
networks.* **Bioinformatics**, 40(8), btae515. <https://doi.org/10.1093/bioinformatics/btae515>

Selected background references:

- Hordijk, W., Smith, J. I., & Steel, M. A. (2015). *Algorithms for detecting and analysing autocatalytic sets.*
  Algorithms for Molecular Biology, 10: 15.
- Steel, M., Xavier, J. C., & Huson, D. H. (2020). *The structure of autocatalytic networks, with application to early
  biochemistry.* J. R. Soc. Interface, 17: 20200488.
- Xavier, J. C., Hordijk, W., Kauffman, S., Steel, M., & Martin, W. F. (2020). *Autocatalytic chemical networks at the
  origin of metabolism.* Proc. R. Soc. B, 287: 20192377.
- Huson, D. H., Xavier, J. C., & Steel, M. A. (2024). *Self‑generating autocatalytic networks: structural results,
  algorithms, and their relevance to early biochemistry.* J. R. Soc. Interface, 21(214): 20230732.
