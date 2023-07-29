package sample;

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
import javafx.scene.shape.Sphere;
import javafx.scene.shape.Cylinder;
import javafx.scene.PointLight;
import javafx.scene.transform.Translate;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.IntegerProperty;
import javafx.animation.AnimationTimer;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javafx.scene.image.ImageView;


public class Main extends Application {

    // ponizej wsp. do wymiarow ekranu symulacji
    private static final float WIDTH = 1200;
    private static final float HEIGHT = 800;

    // ponizej wspolrzedne do tworzenia zooma i myszki
    private double anchorX, anchorY;
    private double anchorAngleX = 0;
    private double anchorAngleY = 0;
    private final DoubleProperty angleX = new SimpleDoubleProperty(10);
    private final DoubleProperty angleY = new SimpleDoubleProperty(0);

    // ponizej wspolrzedne do translacji polozenia plytki z herbem
    private double th1 = -40;
    private double th2 = 133;
    private double th3 = -41.25;

    // macierz do uczenia robota
    int[][] uczenie = new int[3][5000];

    // ponizej wsp. do zapisywania wartosci macierzy oraz do wykorzystania w funkcji LEARNING robota
    int a = 1;
    int b = 1;
    int c = 1;
    int aa=0;
    int bb=0;
    int cc=0;

    // sluzy do wykorzystania w funkcji LEARNING jako miara ottworzenia drogi robota
    int learn = 0;

    //ponizej wsp. do wykrywania kolizji herba z duza plyta PG
    int lewo;
    int prawo;
    int gora;
    int dol;

    // wartosc z jaka predkoscia robot porusza sie (pikseli na krok)
    int szybkosc = 5;


    private long time; // do funkcji timera

    private final IntegerProperty counter1 = new SimpleIntegerProperty(); // krok (aktualne polozenie) 1 ramienia robota
    private final IntegerProperty counter2 = new SimpleIntegerProperty(); // krok (aktualne polozenie) 2 ramienia robota
    private final IntegerProperty counter3 = new SimpleIntegerProperty(); // krok (aktualne polozenie) 3 ramienia robota

    // ponizej zmienne sprawdzaja kiedy bedzie wziety herb zeby pozniej wykonac automatyczna symulacje
    int spra = -1;
    int sprb = -1;
    int sprc = -1;

    // ponizej zmienne sprawdzaja kiedy bedzie puszczony herb zeby pozniej wykonac automatyczna symulacje
    int rpra = -1;
    int rprb = -1;
    int rprc = -1;

    // ponizej zmienne pomagajace przy wykryciu czy mozna zastosowac GRAB/RELEASE
    int gg = 1;
    int rr = 1;

    // ponizej zmienne pomagajace przy wykryciu czy mozna zastosowac GRAB/RELEASE
    boolean g = false;
    boolean r = false;
    boolean h = false; // odpowiednik czy herb jest wziety czy nie
    boolean l = false; // odpowiada na pytanie czy bedzie w ogole learning
    boolean k = false; // zmienna sprawdzajaca jeden z warunkow kolizji
    boolean cof = false; // odpowiada na pytanie czy zaszlo cofanie sie robota

    @Override
    public void start(Stage stage) throws IOException {

        // ponizej SmartGroups do poszczegolnych ramieni robota (ruchomych albo nieruchomych czesci)
        SmartGroup calkiem_nieruchoma = new SmartGroup();
        SmartGroup nieruchoma = new SmartGroup();
        SmartGroup pierwszeRamie = new SmartGroup();
        SmartGroup drugieRamie = new SmartGroup();
        SmartGroup trzecieRamie = new SmartGroup();

        // ponizej funkcje Rotates do poszczegolnych ramieni robota
        Rotate rotate_pierwsze_ramie = new Rotate(0, 0, 0, 0, Rotate.Y_AXIS);
        Rotate rotate_drugie_ramie = new Rotate(0, 30, 30, 0, Rotate.X_AXIS);
        Rotate rotate_trzecie_ramie = new Rotate(0, 30, 30, 50, Rotate.X_AXIS);

        // // ponizej do pewnych SmartGroups sa dodane inne GmartGroups/Groups/swiatla/czesci robota/inne czesci wizualizacji
        calkiem_nieruchoma.getChildren().add(nieruchoma);

        nieruchoma.getChildren().add(pierwszeRamie);
        nieruchoma.getChildren().add(drugieRamie);
        nieruchoma.getChildren().add(trzecieRamie);

        pierwszeRamie.getChildren().add(drugieRamie);
        pierwszeRamie.getChildren().add(trzecieRamie);

        drugieRamie.getChildren().add(trzecieRamie);

        nieruchoma.getChildren().addAll(prepareLightSource1());
        nieruchoma.getChildren().addAll(prepareLightSource1());
        nieruchoma.getChildren().addAll(prepareLightSource2());

        nieruchoma.getChildren().add(preparePodstawa());
        nieruchoma.getChildren().add(prepareSecondPodstawa());
        nieruchoma.getChildren().add(preparePodstawaRobota());
        nieruchoma.getChildren().add(ImageView1());
        nieruchoma.getChildren().add(PGBox());
        nieruchoma.getChildren().add(PGMagnes());
        nieruchoma.getChildren().add(Zasady());
        nieruchoma.getChildren().add(ZasadySlup());
        nieruchoma.getChildren().add(HerbPodstawka());

        // ponizej tworzenie nowych Groups
        Group root1 = new Group(ImageView1());
        Group HERB = new Group(Herb());
        Group przewodnik = new Group(Przewodnik());
        Translate herb_translate = new Translate(th1, th2, th3); //translacja herbu
        HERB.getTransforms().add(herb_translate);

        nieruchoma.getChildren().add(root1);
        nieruchoma.getChildren().add(HERB);
        nieruchoma.getChildren().add(przewodnik);

        pierwszeRamie.getChildren().add(preparePierwszeRamieRobota1()); //<-- grupa pierwsze ramie
        pierwszeRamie.getChildren().add(preparePierwszeRamieRobota2()); //<-- grupa pierwsze ramie
        pierwszeRamie.getChildren().add(preparePierwszeRamieRobota3()); //<-- grupa pierwsze ramie
        drugieRamie.getChildren().add(prepareDrugieRamieRobota1()); //<-- grupa drugie ramie
        trzecieRamie.getChildren().add(prepareTrzecieRamieRobota1()); //<-- grupa drugie ramie
        trzecieRamie.getChildren().add(prepareTrzecieRamieRobota_magnes()); //<-- grupa drugie ramie

        Camera camera = new PerspectiveCamera();
        Scene scene = new Scene(calkiem_nieruchoma, WIDTH, HEIGHT, true);
        scene.setFill(Color.LIGHTSKYBLUE);
        scene.setCamera(camera);


        nieruchoma.translateXProperty().set(WIDTH / 2); // srodek okienka ekranu symulacji
        nieruchoma.translateYProperty().set(HEIGHT / 2 - 70); // srodek okienka ekranu symulacji
        nieruchoma.translateZProperty().set(-800); // ZOOM na samym poczatku

        pierwszeRamie.getTransforms().add(rotate_pierwsze_ramie);
        drugieRamie.getTransforms().add(rotate_drugie_ramie);
        drugieRamie.setRotationAxis(Rotate.X_AXIS);
        trzecieRamie.getTransforms().add(rotate_trzecie_ramie);


        initMouseControl(nieruchoma, scene, stage);

        // KONTROLA WIZUALIZACJI ZA POMOCA KLAWISZOW
        stage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            switch (event.getCode())  {
                case A:
                {
                    obrot_w_lewa_strone_1_ramie(rotate_pierwsze_ramie);

                }
                    break;

                case D:
                {
                    obrot_w_prawa_strone_1_ramie(rotate_pierwsze_ramie);
                }
                    break;

                case W:
                {
                    kierowanie_w_gore_2_ramie(rotate_drugie_ramie);
                }
                break;

                case S:
                {
                    kierowanie_w_dol_2_ramie(rotate_drugie_ramie);
                }
                break;

                case Q:
                {
                    kierowanie_w_gore_3_ramie(rotate_trzecie_ramie);
                }
                break;

                case E:
                {
                    kierowanie_w_dol_3_ramie(rotate_trzecie_ramie);
                }
                break;

                // GRAB object - wez obiekt
                case G:
                {
                    funkcja_grab(nieruchoma,HERB, herb_translate, trzecieRamie);
                }
                break;

                //RELEASE object - pusc obiekt
                case R:
                {
                    funkcja_release(nieruchoma, HERB, herb_translate, trzecieRamie);
                }
                break;

                //C - COFANIE SIE do zera
                case C:
                {
                    if(l!=true)
                    COFANIE_timer(rotate_pierwsze_ramie, rotate_drugie_ramie, rotate_trzecie_ramie);
                }
                break;

                //ZERO - od poczatku
                case Z:
                {
                    //wyzeruj_polozenie(HERB, herb_translate, rotate_pierwsze_ramie, rotate_drugie_ramie, rotate_trzecie_ramie, nieruchoma, trzecieRamie);
                    counter1.set(0);
                    counter2.set(0);
                    counter3.set(0);

                    rotate_pierwsze_ramie.angleProperty().bind(counter1);
                    rotate_drugie_ramie.angleProperty().bind(counter2);
                    rotate_trzecie_ramie.angleProperty().bind(counter3);

                    nieruchoma.getChildren().remove(HERB);
                    trzecieRamie.getChildren().remove(HERB);
                    HERB.setRotationAxis(Rotate.X_AXIS);
                    HERB.setRotate(0);
                    rr = 1;
                    gg = 1;
                    th1 = -40;
                    th2 = 133;
                    th3 = -41.25;
                    herb_translate.setX(th1);
                    herb_translate.setY(th2);
                    herb_translate.setZ(th3);

                    nieruchoma.getChildren().add(HERB);

                }
                break;

                case L:
                {
                    //wyzeruj_polozenie(HERB, herb_translate, rotate_pierwsze_ramie, rotate_drugie_ramie, rotate_trzecie_ramie, nieruchoma, trzecieRamie);
                    //COFANIE_timer(rotate_pierwsze_ramie, rotate_drugie_ramie, rotate_trzecie_ramie);
                    // WROCIC LADNIE DO ZERA I ROZPOCZAC AUTO KIEROWANIE
                    if (cof != true)
                    {
                        l = true;
                        LEARNING_timer(rotate_pierwsze_ramie, rotate_drugie_ramie, rotate_trzecie_ramie, nieruchoma,HERB, herb_translate, trzecieRamie);
                    }
                }

                break;
/*
                case R:
                {
                    AnimationTimer timer = new AnimationTimer() {
                        @Override
                        public void handle(long now) {
                            if (now - time > 30_000_000) {
                                pierwszeRamie.rotateByY(10);
                                rotate_drugie_ramie.angleProperty().bind(counter2);
                                rotate_trzecie_ramie.angleProperty().bind(counter3);
                                if(counter3.get() < 60)
                                    counter3.set((counter3.get() + 5));
                                //counter.set((counter.get() + 10));
                                time = now;
                            }
                        }
                    };
                    timer.start();
                }
                break;*/
            }
        });

        stage.setTitle("Animacja Robota");
        stage.setScene(scene);
        stage.show();

       /* AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - time > 30_000_000) {
                    //counter.set((counter.get() + 10));
                    time = now;
                }
            }
        };
        timer.start();*/
    }


    // SWIATLO Z JEDNEJ STRONY
    private Node[] prepareLightSource1() {
        PointLight pointLight1 = new PointLight();
        pointLight1.setColor(Color.DARKGRAY);
        pointLight1.getTransforms().add(new Translate(500,-150,500));// <-- POBAWIC SIE ZEBY SWIATLO BYLO NORMALNE

        Sphere sphere = new Sphere(2);
        sphere.getTransforms().setAll(pointLight1.getTransforms());
        return new Node[]{pointLight1, sphere};
    }

    private Node prepareLightSource2() {

        AmbientLight ambientLight = new AmbientLight();
        // INNY RODZAJ OSWIETLENIA
        //PointLight pointLight2 = new PointLight();
        //pointLight2.setColor(Color.WHITE);
        //pointLight2.getTransforms().add(new Translate(0,-500,0));// <-- POBAWIC SIE ZEBY SWIATLO BYLO NORMALNE
        //return pointLight2;

        return ambientLight;
    }

    void obrot_w_lewa_strone_1_ramie(Rotate rotate_pierwsze_ramie)
    {
        lewo = 1;
        rotate_pierwsze_ramie.angleProperty().bind(counter1); //<-- ODBLOKOWAĆ JAK BĘDZIE POTRZEBNY RUCH WZGLĘDEM 2 RAMIENIA
        if(counter1.get() < 225 && counter1.get()>=-90 && kolizja() == false)
        {
            uczenie[0][a-1]=(counter1.get());
            counter1.set((counter1.get() + szybkosc));
            uczenie[0][a]=(counter1.get());

            a++;
            lewo = 0;
        }
    }

    void obrot_w_prawa_strone_1_ramie(Rotate rotate_pierwsze_ramie)
    {
        prawo = 1;
        rotate_pierwsze_ramie.angleProperty().bind(counter1); //<-- ODBLOKOWAĆ JAK BĘDZIE POTRZEBNY RUCH WZGLĘDEM 2 RAMIENIA
        if(counter1.get() <= 225 && counter1.get()>-90 && kolizja() == false)
        {
            uczenie[0][a-1]=(counter1.get());
            counter1.set((counter1.get() - szybkosc));
            uczenie[0][a]=(counter1.get());

            a++;
            prawo = 0;
        }
    }

    void kierowanie_w_gore_2_ramie(Rotate rotate_drugie_ramie)
    {
        gora=1;
        rotate_drugie_ramie.angleProperty().bind(counter2); //<-- ODBLOKOWAĆ JAK BĘDZIE POTRZEBNY RUCH WZGLĘDEM 2 RAMIENIA
        if(counter2.get() < 225 && kolizja() == false)
        {
            uczenie[1][b-1]=(counter2.get());
            counter2.set((counter2.get() + szybkosc));
            uczenie[1][b]=(counter2.get());
            b++;
            gora=0;
        }
    }

    void kierowanie_w_dol_2_ramie(Rotate rotate_drugie_ramie)
    {
        dol=1;
        rotate_drugie_ramie.angleProperty().bind(counter2); //<-- ODBLOKOWAĆ JAK BĘDZIE POTRZEBNY RUCH WZGLĘDEM 2 RAMIENIA
        if(counter2.get() > -50 && kolizja() == false)
        {
            uczenie[1][b-1]=(counter2.get());
            counter2.set((counter2.get() - szybkosc));
            uczenie[1][b]=(counter2.get());
            b++;
            dol=0;
        }
    }

    void kierowanie_w_gore_3_ramie(Rotate rotate_trzecie_ramie)
    {
        rotate_trzecie_ramie.angleProperty().bind(counter3);// <-- ODBLOKOWAĆ JAK BĘDZIE POTRZEBNY RUCH WZGLĘDEM 3 RAMIENIA
        if(counter3.get() < 55)
        {
            uczenie[2][c-1]=(counter3.get());
            counter3.set((counter3.get() + szybkosc));
            uczenie[2][c]=(counter3.get());
            c++;
        }
    }

    void kierowanie_w_dol_3_ramie(Rotate rotate_trzecie_ramie)
    {
        rotate_trzecie_ramie.angleProperty().bind(counter3);// <-- ODBLOKOWAĆ JAK BĘDZIE POTRZEBNY RUCH WZGLĘDEM 3 RAMIENIA
        if(counter3.get() > -235)
        {
            uczenie[2][c-1]=(counter3.get());
            counter3.set((counter3.get() - szybkosc));
            uczenie[2][c]=(counter3.get());
            c++;
        }
    }

    // FUNKCJA POZWALAJACA NA WZIECIE HERBU PG
    void funkcja_grab(SmartGroup nieruchoma, Group HERB, Translate herb_translate, SmartGroup trzecieRamie)
    {
        g = true;
        if (counter1.get() <= (-75))
        {
            if (counter2.get() <= (-45))
            {
                if (counter3.get() >=(-140) && counter3.get() <=(-120))
                {
                    k = true;
                    GRAB_HERB(nieruchoma,HERB, herb_translate, trzecieRamie);
                }
            }
        }
        if(counter1.get() <= 5 && counter1.get() >= (-25))
        {
            if(counter2.get() >= 220)
            {
                if(counter3.get() <= (-40) && counter3.get() >=(-50))
                {
                    GRAB_HERB(nieruchoma,HERB, herb_translate, trzecieRamie);
                }
            }
        }
    }

    // FUNKCJA POZWALAJACA NA PUSZCZENIE HERBU PG
    void funkcja_release(SmartGroup nieruchoma, Group HERB, Translate herb_translate, SmartGroup trzecieRamie)
    {
        r = true;
        if (counter1.get() <= 5 && counter1.get() >= (-5))
        {
            if (counter2.get() <= 5 && counter2.get() >= (-5))
            {
                if (counter3.get() <= (-85) && counter3.get() >= (-95))
                {
                    RELEASE_HERB(nieruchoma, HERB, herb_translate, trzecieRamie);
                    r=false;
                    k = false;
                }
            }
        }
    }

    // FUNKCJA WYKRYWAJACA KOLIZJE HERBU Z DUZA PLYTA PG
    boolean kolizja()
    {
        boolean kolizja = false;

        if(k == true && ((counter1.get() == 5 && lewo==1) || (counter1.get() == 50 && prawo==1)))
        {

            if(counter2.get() >= (-30) && counter2.get() <=(30))
            {
                kolizja = true;
                prawo =0;
                lewo=0;
                gora=0;
                dol=0;
            }
        }
        else if ((k == true && counter1.get()>5 && counter1.get()<50))
        {
            if ((counter2.get() == (-35) && gora ==1) || ( counter2.get() ==(35) && dol ==1))
            {
                kolizja = true;
                prawo =0;
                lewo=0;
                gora=0;
                dol=0;
            }
        }
        return kolizja;
    }

    //FUNKCJA WYKOYWANIA UCZONYCH RUCHOW ROBOTA
    void LEARNING_timer(Rotate rotate_pierwsze_ramie, Rotate rotate_drugie_ramie, Rotate rotate_trzecie_ramie, SmartGroup nieruchoma, Group HERB, Translate herb_translate, SmartGroup trzecieRamie )
    {
        //    WYZERUJ POLOZENIE
        {
            counter1.set(0);
            counter2.set(0);
            counter3.set(0);

            rotate_pierwsze_ramie.angleProperty().bind(counter1);
            rotate_drugie_ramie.angleProperty().bind(counter2);
            rotate_trzecie_ramie.angleProperty().bind(counter3);

            nieruchoma.getChildren().remove(HERB);
            trzecieRamie.getChildren().remove(HERB);
            HERB.setRotationAxis(Rotate.X_AXIS);
            HERB.setRotate(0);
            rr = 1;
            gg = 1;
            th1 = -40;
            th2 = 133;
            th3 = -41.25;
            herb_translate.setX(th1);
            herb_translate.setY(th2);
            herb_translate.setZ(th3);

            nieruchoma.getChildren().add(HERB);
        }

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - time > 30_000_000) {

                    // LEARNING - funkcja powtarza droge ktora przeszkismy w trakcie uczenia robota
                    {
                        if (((uczenie[0][aa]) != spra && spra != 0) && (aa != rpra && rpra != 0)) {
                            if (aa < a) {
                                rotate_pierwsze_ramie.angleProperty().bind(counter1);
                                counter1.set((uczenie[0][aa]));
                                aa++;
                            }
                        } else if ((uczenie[0][aa]) == spra || spra == 0) {
                            rotate_pierwsze_ramie.angleProperty().bind(counter1);
                            counter1.set((uczenie[0][aa]));
                            spra = 0;
                        } else if (aa == rpra || rpra == 0) {
                            rotate_pierwsze_ramie.angleProperty().bind(counter1);
                            counter1.set((uczenie[0][aa]));
                            rpra = 0;
                        }

                        //--------
                        if (((uczenie[1][bb]) != sprb && sprb != 0) && (bb != rprb && rprb != 0)) {
                            if (bb < b) {
                                rotate_drugie_ramie.angleProperty().bind(counter2);
                                counter2.set((uczenie[1][bb]));
                                bb++;
                            }
                        } else if ((uczenie[1][bb]) == sprb || sprb == 0) {
                            rotate_drugie_ramie.angleProperty().bind(counter2);
                            counter2.set((uczenie[1][bb]));
                            sprb = 0;
                        } else if (bb == rprb || rprb == 0) {
                            rotate_drugie_ramie.angleProperty().bind(counter2);
                            counter2.set((uczenie[1][bb]));
                            rprb = 0;
                        }

                        //-----------------
                        if (((uczenie[2][cc]) != sprc && sprc != 0) && (cc != rprc && rprc != 0)) {
                            if (cc < c) {
                                rotate_trzecie_ramie.angleProperty().bind(counter3);
                                counter3.set((uczenie[2][cc]));
                                cc++;
                            }
                        } else if ((uczenie[2][cc]) == sprc || sprc == 0) {
                            rotate_trzecie_ramie.angleProperty().bind(counter3);
                            counter3.set((uczenie[2][cc]));
                            sprc = 0;
                        } else if (cc == rprc || rprc == 0) {
                            rotate_trzecie_ramie.angleProperty().bind(counter3);
                            counter3.set((uczenie[2][cc]));
                            rprc = 0;
                        }


                        if (spra == 0 && sprb == 0 && sprc == 0 && h == true) {
                            g = true;
                            GRAB_HERB(nieruchoma, HERB, herb_translate, trzecieRamie);
                            h = false;
                            spra = -1;
                            sprb = -1;
                            sprc = -1;
                        }
                        if (rpra == 0 && rprb == 0 && rprc == 0) {
                            r = true;
                            RELEASE_HERB(nieruchoma, HERB, herb_translate, trzecieRamie);

                            counter1.set((uczenie[0][aa - 1]));
                            counter2.set((uczenie[1][bb - 1]));
                            counter3.set((uczenie[2][cc - 1]));
                            rpra = -1;
                            rprb = -1;
                            rprc = -1;
                        }
                        learn++;
                        time = now;
                    }
                }
            }
        };
        timer.start();
        learn = 0;

    }

    //FUNKCJA COFANIA ROBOTA DO POZYCJI POCZATKOWEJ
    void COFANIE_timer(Rotate rotate_pierwsze_ramie, Rotate rotate_drugie_ramie, Rotate rotate_trzecie_ramie)
    {
        cof = true;
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - time > 30_000_000)
                {

                    if (counter1.get() > 0 && learn < a) {
                        rotate_pierwsze_ramie.angleProperty().bind(counter1);
                        counter1.set(counter1.get() - 5);
                    }

                    if (counter1.get() < 0 && learn < a) {
                        rotate_pierwsze_ramie.angleProperty().bind(counter1);
                        counter1.set(counter1.get() + 5);
                    }

                    if (counter2.get() > 0 && learn < b) {
                        rotate_drugie_ramie.angleProperty().bind(counter2);
                        counter2.set(counter2.get() - 5);
                    }

                    if (counter2.get() < 0 && learn < b) {
                        rotate_drugie_ramie.angleProperty().bind(counter2);
                        counter2.set(counter2.get() + 5);
                    }

                    if (counter3.get() > 0 && learn < c) {
                        rotate_trzecie_ramie.angleProperty().bind(counter3);
                        counter3.set(counter3.get() - 5);
                    }

                    if (counter3.get() < 0 && learn < c) {
                        rotate_trzecie_ramie.angleProperty().bind(counter3);
                        counter3.set(counter3.get() + 5);
                    }

                    // COFANIE SIE TAKĄ SAMĄ DROGĄ CO PZESZEDŁ DO PRZODU
                                /*

                                if (learn < a+1) {
                                    rotate_pierwsze_ramie.angleProperty().bind(counter1); //<-- ODBLOKOWAĆ JAK BĘDZIE POTRZEBNY RUCH WZGLĘDEM 2 RAMIENIA
                                    counter1.set((uczenie[0][a-learn]));
                                }
                                if (learn < b+1) {
                                    rotate_drugie_ramie.angleProperty().bind(counter2); //<-- ODBLOKOWAĆ JAK BĘDZIE POTRZEBNY RUCH WZGLĘDEM 2 RAMIENIA
                                    counter2.set((uczenie[1][b-learn]));
                                }
                                if (learn < c+1) {
                                    rotate_trzecie_ramie.angleProperty().bind(counter3); //<-- ODBLOKOWAĆ JAK BĘDZIE POTRZEBNY RUCH WZGLĘDEM 2 RAMIENIA
                                    counter3.set((uczenie[2][c-learn]));}

                                 */
                    learn++;
                    time = now;
                }
            }
        };
        learn =0;
        timer.start();
    }

    // czesci funkcji funkcjsa_release()
    void RELEASE_HERB(SmartGroup nieruchoma, Group HERB, Translate herb_translate, SmartGroup trzecieRamie )
    {
        l = true;
        r = true;

        if (r == true& rr == 1)
        {
            trzecieRamie.getChildren().remove(HERB);

            th1 =-40;
            th2 = -118;
            th3 = 30;
            herb_translate.setX(th1);
            herb_translate.setY(th2);
            herb_translate.setZ(th3);
            HERB.setRotationAxis(Rotate.X_AXIS);
            HERB.setRotate(-90);

            nieruchoma.getChildren().add(HERB);
            rr = 0;
            gg = 1;

            rpra = a;
            rprb = b;
            rprc = c;

            r = false;
        }
    }

    // czesc funkcji funkcja_grab();
    void GRAB_HERB(SmartGroup nieruchoma, Group HERB, Translate herb_translate, SmartGroup trzecieRamie )
    {
        h = true;
        g = true;

        if(g == true & gg==1)
        {
            nieruchoma.getChildren().remove(HERB);

            if (rr ==0)
            {
                HERB.setRotationAxis(Rotate.X_AXIS);
                HERB.setRotate(0);
            }

            th2 = -30;
            th3 = 50;
            herb_translate.setX(th1);
            herb_translate.setY(th2);
            herb_translate.setZ(th3);

            trzecieRamie.getChildren().add(HERB);
            gg = 0;
            rr = 1;
        }

        if (h == true)
        {
            spra = uczenie[0][a]=(counter1.get());
            sprb = uczenie[1][b]=(counter2.get());
            sprc = uczenie[2][c]=(counter3.get());
        }

        g = false;

    }

    void wyzeruj_polozenie(Group HERB, Translate herb_translate, Rotate rotate_pierwsze_ramie, Rotate rotate_drugie_ramie, Rotate rotate_trzecie_ramie, SmartGroup nieruchoma, SmartGroup trzecieRamie) throws IOException{

        counter2.set(0);
        counter3.set(0);

        rotate_pierwsze_ramie.angleProperty().bind(counter1);
        rotate_drugie_ramie.angleProperty().bind(counter2);
        rotate_trzecie_ramie.angleProperty().bind(counter3);

        nieruchoma.getChildren().remove(HERB);
        trzecieRamie.getChildren().remove(HERB);
        HERB.setRotationAxis(Rotate.X_AXIS);
        HERB.setRotate(0);
        rr = 1;
        gg = 1;
        th1 = -40;
        th2 = 133;
        th3 = -41.25;
        herb_translate.setX(th1);
        herb_translate.setY(th2);
        herb_translate.setZ(th3);

        nieruchoma.getChildren().add(HERB);

    }

    // PONIZEJ SA UMIESZCZONE WSZYSTKIE 3D OBIEKTY KTORE SA W WIZUALIZACJI 3D

    //GROUND (ziemia)
    private Cylinder preparePodstawa() throws IOException{

        Cylinder ziemia = new Cylinder(500, 1, 500);
        ziemia.getTransforms().add(new Translate(0, 135, 0));
        InputStream stream = new FileInputStream("C:\\Users\\lk\\Desktop\\Studia\\POGK\\przebieg_pracy_180120_183022\\grafika3D\\src\\ground3.jpg");
        Image image = new Image(stream);
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseMap(image);
        ziemia.setMaterial(material);

        return ziemia;
    }

    // PODSTAWA ROBOTA
    private Cylinder prepareSecondPodstawa() {
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.DARKGREY);

        Cylinder podstawa = new Cylinder(20, 10, 20);
        podstawa.setMaterial(material);
        podstawa.getTransforms().add(new Translate(0, 130, 0));

        return podstawa;
    }

    private Cylinder preparePodstawaRobota() {
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.DARKGRAY);

        Cylinder podstawa_robota = new Cylinder(10, 100, 10);
        podstawa_robota.setMaterial(material);
        podstawa_robota.getTransforms().add(new Translate(0, 90, 0));

        return podstawa_robota;
    }

    // 1 RAMIE ROBOTA
    private Cylinder preparePierwszeRamieRobota1() {
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.ROYALBLUE);

        Cylinder ramie_robota_1 = new Cylinder(10, 20, 10);
        ramie_robota_1.setMaterial(material);
        ramie_robota_1.getTransforms().add(new Translate(0, 30, 0));

        return ramie_robota_1;
    }

    private Cylinder preparePierwszeRamieRobota2() {
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.ROYALBLUE);

        Cylinder ramie_robota_1 = new Cylinder(10, 42.5, 10);
        ramie_robota_1.setMaterial(material);
        ramie_robota_1.getTransforms().add(new Translate(30, 21, 0));
        ramie_robota_1.setRotate(90);

        return ramie_robota_1;
    }

    private Cylinder preparePierwszeRamieRobota3() {
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.ROYALBLUE);

        Cylinder ramie_robota_1 = new Cylinder(15, 5, 15);
        ramie_robota_1.setMaterial(material);
        ramie_robota_1.getTransforms().add(new Translate(30, 45, 0));
        ramie_robota_1.setRotate(90);

        return ramie_robota_1;
    }

    // 2 RAMIE ROBOTA
    private Box prepareDrugieRamieRobota1() {
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.ROYALBLUE);

        Box ramie_robota_2 = new Box(30.5, 10, 80);
        ramie_robota_2.setMaterial(material);
        ramie_robota_2.getTransforms().add(new Translate(30, 50, 22));
        ramie_robota_2.setRotate(90);

        return ramie_robota_2;
    }

    private Box prepareTrzecieRamieRobota1() {
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.ROYALBLUE);

        Box ramie_robota_3 = new Box(57, 10, 18);
        ramie_robota_3.setMaterial(material);
        ramie_robota_3.getTransforms().add(new Translate(10, 40, 50));
        ramie_robota_3.setRotate(90);

        return ramie_robota_3;
    }

    private Box prepareTrzecieRamieRobota_magnes() {
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.BLACK);

        Box ramie_robota_3 = new Box(10, 10, 10);
        ramie_robota_3.setMaterial(material);
        ramie_robota_3.getTransforms().add(new Translate(-23.5, 40, 50));
        ramie_robota_3.setRotate(90);

        return ramie_robota_3;

    }

    //DUŻY PG BOX BEZ PLYTKI "PG"
    private Box PGBox() {
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.GHOSTWHITE);
        Box box = new Box(170, 150, 60);
        box.setMaterial(material);
        box.getTransforms().add(new Translate(-43, 60, 150));

        return box;
    }

    private ImageView ImageView1() throws IOException{
        InputStream stream = new FileInputStream("C:\\Users\\lk\\Desktop\\Studia\\POGK\\przebieg_pracy_180120_183022\\grafika3D\\src\\Herb-PG_bt.gif");
        Image image = new Image(stream);
        ImageView imageView = new ImageView();
        imageView.setImage(image);
        imageView.setX(-115);
        imageView.setY(-1);
        imageView.setTranslateZ(119);
        imageView.setFitWidth(150);
        imageView.setPreserveRatio(true);

        return imageView;
    }

    private Box PGMagnes(){
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.BLACK);
        Box box = new Box(15, 15, 3);
        box.setMaterial(material);
        box.getTransforms().add(new Translate(-40, 30, 120));
        return box;
    }

    // PREWODNIK (zaady) - TABELKA
    private Box Zasady(){
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.DARKGRAY);
        Box box = new Box(110, 80, 10);
        box.setMaterial(material);
        box.getTransforms().add(new Translate(130, 50, 150));
        box.setRotationAxis(Rotate.Y_AXIS);
        box.setRotate(45);

        return box;
    }

    private Box ZasadySlup(){
        PhongMaterial material = new PhongMaterial();
        Color pg_color = Color.rgb(0,58,106);//106
        material.setDiffuseColor(pg_color);
        Box box = new Box(20, 50, 8);
        box.setMaterial(material);
        box.getTransforms().add(new Translate(131, 113, 150));
        box.setRotationAxis(Rotate.Y_AXIS);
        box.setRotate(45);

        return box;
    }

    private ImageView Przewodnik() throws IOException{
        InputStream stream = new FileInputStream("C:\\Users\\lk\\Desktop\\Studia\\POGK\\przebieg_pracy_180120_183022\\grafika3D\\src\\przewodnik_1.bmp");
        Image image = new Image(stream);
        ImageView imageView = new ImageView();
        imageView.setImage(image);
        imageView.setFitWidth(110);
        imageView.getTransforms().add(new Translate(91, 10, 105.5));
        imageView.setRotationAxis(Rotate.Y_AXIS);
        imageView.setRotate(45);
        imageView.setPreserveRatio(true);

        return imageView;
    }

    // MALA PLYTKA Z NAPISEM "PG"
    private Box Herb() throws IOException{

        InputStream stream = new FileInputStream("C:\\Users\\lk\\Desktop\\Studia\\POGK\\przebieg_pracy_180120_183022\\grafika3D\\src\\herb_bmp.bmp");
        Image image = new Image(stream);
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseMap(image);
        Box box = new Box(40, 3, 36);
        box.setMaterial(material);

        return box;
    }

    private Box HerbPodstawka() {
        PhongMaterial material = new PhongMaterial();
        Color brown_color = Color.rgb(106,58,0);//106
        material.setDiffuseColor(brown_color);
        Box box = new Box(50, 4, 50);
        box.setMaterial(material);
        box.getTransforms().add(new Translate(-41, 136, -41));

        return box;
    }


    // MOUSE ROTATION X, Y, MOUSE CONTROL
    private void initMouseControl(SmartGroup group, Scene scene, Stage stage) throws IOException{
        Rotate xRotate;
        Rotate yRotate;
        group.getTransforms().addAll(
                xRotate = new Rotate(0, Rotate.X_AXIS),
                yRotate = new Rotate(0, Rotate.Y_AXIS)
        );
        xRotate.angleProperty().bind(angleX);
        yRotate.angleProperty().bind(angleY);

        scene.setOnMousePressed(event -> {
            anchorX = event.getSceneX();
            anchorY = event.getSceneY();
            anchorAngleX = angleX.get();
            anchorAngleY = angleY.get();
        });

        // OGRANICZENIA HORYZONTU
        scene.setOnMouseDragged(event -> {
            double horyzont = anchorAngleX - (anchorY - event.getSceneY()); // OGRANICZENIA HORYZONTU
            if(horyzont >3 & horyzont <177)
            angleX.set(horyzont);
            angleY.set(anchorAngleY + anchorX - event.getSceneX());
        });

        // ZOOM
        stage.addEventHandler(ScrollEvent.SCROLL, event -> {
            double delta = event.getDeltaY();
            double zoom = group.getTranslateZ()+ delta;
            if((zoom < -700) & (zoom > -1100)) // ograniczenia ZOOM myszki
                group.translateZProperty().set(zoom);
        });
    }

    public static void main(String[] args) throws IOException{
        launch(args);
    }

    class SmartGroup extends Group {
        Rotate r;
        Transform t = new Rotate();

        void rotateByX(int ang) {
            r = new Rotate(ang, Rotate.X_AXIS);
            t = t.createConcatenation(r);
            this.getTransforms().clear();
            this.getTransforms().addAll(t);
        }

        void rotateByY(int ang) {
            r = new Rotate(ang, Rotate.Y_AXIS);
            t = t.createConcatenation(r);
            this.getTransforms().clear();
            this.getTransforms().addAll(t);
        }
    }
}