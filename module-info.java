/**
 * 
 */
/**
 * 
 */
module Gallery {
	opens Gallery to javafx.graphics, javafx.controls, java.sql;
	requires javafx.graphics;
	requires java.sql;
	requires javafx.controls;
	requires java.desktop;
	requires javafx.swing;
}