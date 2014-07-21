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

<h3>Class Diagram</h3>
![ScreenShot](https://github.com/suken/UmlGeneratorTool/blob/master/UmlGeneratorTool/icons/ClassDiagramIcon.png)
The class diagram generates the followings:<br>
<ul>
  <li>Fields if {@code fieldsIncluded} set to TRUE
  <li>Methods if {@code methodIncluded} set TRUE
  <li>Parent class depedencies
  <li>Implemented interfaces
  <li>Composite class dependencies
</ul>
![ScreenShot](https://github.com/suken/UmlGeneratorTool/blob/master/Resources/SampleClassDiagram.png)

<h3> Spring Dependency Diagram </h3>
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
![ScreenShot](https://github.com/suken/UmlGeneratorTool/blob/master/Resources/SampleSpringDependencyDiagram.png)



<h3> JPA Mapping diagram</h3>
![ScreenShot](https://github.com/suken/UmlGeneratorTool/blob/master/UmlGeneratorTool/icons/JPAIcon.png)
The JPA mapping diagram utility can be used to identify the relationship between persistent classes. The utility generates the following:
<ul>
  <li> Persistent entity types (ENTITY, TABLE or MAPPED SUPER CLASSES)
  <li> Inheritance of persistent entities.
  <li> Mapped relationships (OntToOne, ManyToOne, OneToMany)
  <li> Database table name
  <li> Mapped database columns
  <li> Identifier columns
</ul>
Following is the sample generated JPA mapping diagram.
![ScreenShot](https://github.com/suken/UmlGeneratorTool/blob/master/Resources/SampleJPAMappingDiagram.png)


<h3>Component Diagram (Maven)</h3>
![ScreenShot](https://github.com/suken/UmlGeneratorTool/blob/master/UmlGeneratorTool/icons/ComponentDiagramIcon.png)
The method recursively inspects the given source directory to parse all POM files. <br>
![ScreenShot](https://github.com/suken/UmlGeneratorTool/blob/master/Resources/SampleComponentDiagram.png)

<br>
<b> Warning </b><br>
If the component diagram is too complicated then the GraphViz may not generate the PNG file. Try opening the plantuml file in plantuml eclipse plugin.


UmlGeneratorTool
==================
The UmlGeneratorTool is an Eclipse plugin to allow developers to generate UML diagrams from eclipse projects. The Eclipse plugin is compatible with Eclipse 3.5+ distributions. If you are using older version of Eclipse than God Bless You ;)

<h3> Installation Guilde </h3>
Under construction. I am still setting up the maven repository for the two UML projects. Its too early for me to start releasing versions officially.
But if you really feel like trying the tool out then simply import UmlGeneratorTool project in you eclipse and then export it as "Deployable plug-ins and fragments" into your eclipse plugin directory.

<h3> How to Generate UML diagrams </h3>
All the available UML diagrams can be generated from project contextual menu. Here is a screen shot:<br>
![ScreenShot](https://github.com/suken/UmlGeneratorTool/blob/master/Resources/UmlGeneratorMenu.png)

Each diagram has its own options for UML generation. Please refer to the following:<br>

<b>Class Diagram and Spring Depedency Diagram</b><br>
<img align="center" src="https://github.com/suken/UmlGeneratorTool/blob/master/Resources/ClassAndSpringDiagramOptionsDialog.png"/>

<b>Component Diagram and JPA Mapping diagram</b><br>
<img align="center" src="https://github.com/suken/UmlGeneratorTool/blob/master/Resources/ComponentDiagramOptionsDialog.png"/>
