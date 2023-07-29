//SPRAWDZANIE
package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.*;
import javafx.scene.AmbientLight;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.Cylinder;
import javafx.scene.PointLight;
import javafx.scene.transform.Translate;
import javafx.geometry.Point3D;
import javafx.scene.input.MouseEvent;
import javafx.scene.Node;
import javafx.scene.input.PickResult;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.IntegerProperty;
import javafx.scene.layout.StackPane;
import javafx.animation.AnimationTimer;


public class sprawdzenie extends Application {

    private long time;
    private final IntegerProperty counter = new SimpleIntegerProperty();

    @Override
    public void start(Stage primaryStage) {
        Rotate rotate1 = new Rotate(0, 0, 0, 0, Rotate.Z_AXIS);
        rotate1.angleProperty().bind(counter);

        Box box1 = new Box(100, 100, 100);
        box1.setMaterial(new PhongMaterial(Color.LIMEGREEN));
        box1.getTransforms().add(rotate1);

        Cylinder axis1 =  new Cylinder(2, 200);
        axis1.setRotationAxis(Rotate.X_AXIS);
        axis1.setRotate(90);
        axis1.setMaterial(new PhongMaterial(Color.BLUE));

        Group group1 = new Group(box1, axis1);
        group1.setTranslateX(-100);

        Rotate rotate2 = new Rotate(0, 50, 50, 0, Rotate.Z_AXIS);
        rotate2.angleProperty().bind(counter);

        Box box2 = new Box(100, 100, 100);
        box2.setMaterial(new PhongMaterial(Color.LAVENDER));
        box2.getTransforms().add(rotate2);

        Cylinder axis2 =  new Cylinder(2, 200);
        axis2.setRotationAxis(Rotate.X_AXIS);
        axis2.setRotate(90);
        axis2.setTranslateX(-50);
        axis2.setTranslateY(-50);
        axis2.setMaterial(new PhongMaterial(Color.RED));

        Group group2 = new Group(box2, axis2);
        group2.setTranslateX(200); // <-- peredvigaet box2 na osiach (os X -> 200)

        Group root = new Group(group1, group2);

        SubScene subScene = new SubScene(root, 600, 400, true, SceneAntialiasing.BALANCED);
        PerspectiveCamera camera = new PerspectiveCamera();
        camera.setTranslateX(-200);
        camera.setTranslateY(-200);
        camera.setTranslateZ(-100);
        camera.setRotationAxis(new Point3D(0, 0.5, 0.5));
        camera.setRotate(10);
        subScene.setCamera(camera);
        Scene scene = new Scene(new StackPane(subScene), 600, 400, true, SceneAntialiasing.BALANCED);

        primaryStage.setScene(scene);
        primaryStage.show();

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - time > 30_000_000) {
                    counter.set((counter.get() + 1) % 360);
                    time = now;
                }
            }
        };
        timer.start();
    }
}


    /*
     From fx83dfeatures.Camera3D
     http://hg.openjdk.java.net/openjfx/8u-dev/rt/file/5d371a34ddf1/apps/toys/FX8-3DFeatures/src/fx83dfeatures/Camera3D.java

     */


    /*

 public class sprawdzenie extends Application {

    private final Group root = new Group();
    private PerspectiveCamera camera;
    private final double sceneWidth = 800;
    private final double sceneHeight = 600;

    private double mousePosX;
    private double mousePosY;
    private double mouseOldX;
    private double mouseOldY;
    private final Rotate rotateX = new Rotate(-20, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(-20, Rotate.Y_AXIS);

    private volatile boolean isPicking=false;
    private Point3D vecIni, vecPos;
    private double distance;
    private Sphere s;

  public void start(Stage stage) {
        Box floor = new Box(1500, 10, 1500);
        floor.setMaterial(new PhongMaterial(Color.GRAY));
        floor.setTranslateY(150);
        root.getChildren().add(floor);

        Sphere sphere = new Sphere(150);
        sphere.setMaterial(new PhongMaterial(Color.RED));
        sphere.setTranslateY(-5);
        root.getChildren().add(sphere);

        Scene scene = new Scene(root, sceneWidth, sceneHeight, true, SceneAntialiasing.BALANCED);
        scene.setFill(Color.web("3d3d3d"));

        camera = new PerspectiveCamera(true);
        camera.setVerticalFieldOfView(false);

        camera.setNearClip(0.1);
        camera.setFarClip(100000.0);
        camera.getTransforms().addAll (rotateX, rotateY, new Translate(0, 0, -3000));

        PointLight light = new PointLight(Color.GAINSBORO);
        root.getChildren().add(light);
        root.getChildren().add(new AmbientLight(Color.WHITE));
        scene.setCamera(camera);

        scene.setOnMousePressed((MouseEvent me) -> {
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            PickResult pr = me.getPickResult();
            if(pr!=null && pr.getIntersectedNode() != null && pr.getIntersectedNode() instanceof Sphere){
                distance=pr.getIntersectedDistance();
                s = (Sphere) pr.getIntersectedNode();
                isPicking=true;
                vecIni = unProjectDirection(mousePosX, mousePosY, scene.getWidth(),scene.getHeight());
            }
        });
        scene.setOnMouseDragged((MouseEvent me) -> {
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            if(isPicking){
                vecPos = unProjectDirection(mousePosX, mousePosY, scene.getWidth(),scene.getHeight());
                Point3D p=vecPos.subtract(vecIni).multiply(distance);
                s.getTransforms().add(new Translate(p.getX(),p.getY(),p.getZ()));
                vecIni=vecPos;
                PickResult pr = me.getPickResult();
                if(pr!=null && pr.getIntersectedNode() != null && pr.getIntersectedNode()==s){
                    distance=pr.getIntersectedDistance();
                } else {
                    isPicking=false;
                }
            } else {
                rotateX.setAngle(rotateX.getAngle()-(mousePosY - mouseOldY));
                rotateY.setAngle(rotateY.getAngle()+(mousePosX - mouseOldX));
                mouseOldX = mousePosX;
                mouseOldY = mousePosY;
            }
        });
        scene.setOnMouseReleased((MouseEvent me)->{
            if(isPicking){
                isPicking=false;
            }
        });

        stage.setTitle("3D Dragging");
        stage.setScene(scene);
        stage.show();
    }}

    /*
     From fx83dfeatures.Camera3D
     http://hg.openjdk.java.net/openjfx/8u-dev/rt/file/5d371a34ddf1/apps/toys/FX8-3DFeatures/src/fx83dfeatures/Camera3D.java


     */

/*
//sprawdzenie
package sample;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws FileNotFoundException {
        //Creating an image
        Image image = new Image(new FileInputStream("http://www.pg.gda.pl/chem/CEEAM/Grafika/Herb-PG_bt.gif"));

        //Setting the image view
        ImageView imageView = new ImageView(image);

        //Setting the position of the image
        imageView.setX(50);
        imageView.setY(25);

        //setting the fit height and width of the image view
        imageView.setFitHeight(455);
        imageView.setFitWidth(500);

        //Setting the preserve ratio of the image view
        imageView.setPreserveRatio(true);

        //Creating a Group object
        Group root = new Group(imageView);

        //Creating a scene object
        Scene scene = new Scene(root, 600, 500);

        //Setting title to the Stage
        stage.setTitle("Loading an image");

        //Adding scene to the stage
        stage.setScene(scene);

        //Displaying the contents of the stage
        stage.show();
    }
    public static void main(String args[]) {
        launch(args);
    }
}
 */
