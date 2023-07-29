package sample;

import javafx.scene.Camera;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.*;
import javafx.scene.AmbientLight;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;
import javafx.scene.shape.Box;
import javafx.scene.text.Text;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.Cylinder;
import javafx.scene.PointLight;
import javafx.scene.transform.Translate;
import javafx.scene.control.Button;
import javafx.scene.transform.Rotate;
import javafx.geometry.Point3D;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.IntegerProperty;
import javafx.scene.layout.StackPane;
import javafx.animation.AnimationTimer;
import javafx.scene.text.Font;
import javafx.scene.control.Slider;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javafx.scene.image.ImageView;
import javafx.scene.paint.ImagePattern;
import javafx.scene.control.Button;
import javafx.scene.shape.Shape;
//import  javafx.scene.SubScene.setFill;

public class Controller  {
    public Button button;
    //public Button button;
    public SubScene subsceneee;//(globe.getRoot(), WIDTH, HEIGHT);
    public Camera camera = new PerspectiveCamera();

        public void start(Stage stage) throws IOException {


            subsceneee.setFill(Color.LIGHTSKYBLUE);

            //subsceneee.setCamera(camera);
        }


    public void handleButtonClick(){
        System.out.println("Run some code the user doesn't see");
        button.setText("Stop touching me");
    }
}
