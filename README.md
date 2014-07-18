UmlGenerator
============
<p>
UML Generator provides APIs for generating UML diagrams from java source. The UML Generator uses plantuml and graphviz liraries for generating diagrams. The utility outputs UML in following format files:
<ul>
<li> Plantuml text file (.plantuml) - Any plantuml standard viewer can be used to view the UML diagram.<br>
<li> Diagram UML file (.png)
</ul>
</p>

The library supports following UML diagrams:

<h2>Class Diagram</h2>
![ScreenShot](https://github.com/suken/UmlGeneratorTool/blob/master/UmlGeneratorTool/icons/ClassDiagramIcon.png)
The class diagram generates the followings:<br>
<ul>
  <li>Fields if {@code fieldsIncluded} set to TRUE
  <li>Methods if {@code methodIncluded} set TRUE
  <li>Parent class depedencies
  <li>Implemented interfaces
  <li>Composite class dependencies
</ul>
![ScreenShot](https://github.com/suken/UmlGeneratorTool/blob/master/UmlGenerator/samples/SampleClassDiagram.png)

<h2> Spring Dependency Diagram </h2>
![ScreenShot](https://github.com/suken/UmlGeneratorTool/blob/master/UmlGeneratorTool/icons/SpringIcon.png)
In addition to plain class diagram, the spring class diagram also generates followings:
<ul>
  <li> Autowired depedencies
  <li> Required depedencies
  <li> Resource depedencies
  <li> Component classes
  <li> Controller classes
  <li> Service classes
  <li> Repository classes
  <li> Bean classes
  <li> Configuration classes
  <li> Additional comments are provided for class level annotations.
</ul>
![ScreenShot](https://github.com/suken/UmlGeneratorTool/blob/master/UmlGenerator/samples/SampleSpringDependencyDiagram.png)


<h2>Component Diagram (Maven)</h2>
![ScreenShot](https://github.com/suken/UmlGeneratorTool/blob/master/UmlGeneratorTool/icons/ComponentDiagramIcon.png)
The method recursively inspects the given source directory to parse all POM files. <br>
![ScreenShot](https://github.com/suken/UmlGeneratorTool/blob/master/UmlGenerator/samples/SampleComponentDiagram.png)

<br>
<b> Warning </b><br>
If the component diagram is too complicated then the GraphViz may not generate the PNG file. Try opening the plantuml file in plantuml eclipse plugin.
