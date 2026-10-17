package _5090;

import javafx.animation.*;
import javafx.scene.Group;
import javafx.scene.effect.Glow;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.util.Duration;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// the star is created by loading an obj file, reading it, parse it to JavaFX mesh.
public class Star {

    private MeshView mesh;
    private Group parent;


    public Star(Group root, File objFile, int laneIndex, double hitTime) {
        this.parent = root; // needed to add the star to group

        double xPos = laneIndex * 200 + 100; // same x coord as the selected lane
        double yStart = -200; // star begins outside the screen
        double yEnd = 400; // top surface of lane
        double fallDurationSec = 1.0; // 1 sec to fall

        double delayBeforeFall = hitTime - fallDurationSec + 0.5; // we want star to sync at the moment it touches box and there is a visual delay, 0.2s tested to be optimal
        if (delayBeforeFall < 0) delayBeforeFall = 0; // not gonna happen unless in testing

        // have the star pause until it's its turn to fall
        PauseTransition pause = new PauseTransition(Duration.seconds(delayBeforeFall));
        pause.setOnFinished(ev -> {
            mesh = loadObjToMesh(objFile); // load the mesh
            if (mesh == null) return;

            // Apply glow effect
            Glow glow = new Glow(1.0); // 0 = no glow, 1 = max glow
            mesh.setEffect(glow);

            // let the star begin high above and at the center of the box
            mesh.setTranslateX(xPos);
            mesh.setTranslateZ(-50);
            root.getChildren().add(mesh);

            // falling animation
            TranslateTransition fall = new TranslateTransition(Duration.seconds(fallDurationSec), mesh);
            fall.setFromY(yStart);
            fall.setToY(yEnd);
            fall.setCycleCount(1); // fall once
            fall.setOnFinished(e -> parent.getChildren().remove(mesh)); // clean up
            fall.play();

            // Rotation on random axis
            Random rand = new Random();
            double ax = rand.nextDouble();
            double ay = rand.nextDouble();
            double az = rand.nextDouble();

            RotateTransition rotate = new RotateTransition(Duration.seconds(fallDurationSec), mesh);
            rotate.setByAngle(360+ rand.nextDouble() * 360); // 360 to 720
            rotate.setAxis(new javafx.geometry.Point3D(ax, ay, az)); // rotate around all axes
            rotate.setCycleCount(Animation.INDEFINITE); // infinitely rotates
            rotate.play();
        });
        pause.play();
    }

    // obj file is a file that contains info about a 3d object. It includes coordinates of vertices, texture coordinates, and polygonal face connectivity, as well as information about materials
    // here we only use vertices and faces, which are the two required data to make a 3d model.
    private MeshView loadObjToMesh(File objFile) {
        // meshes are created by triangles
        TriangleMesh mesh = new TriangleMesh();
        // vertex positions include xyz coords
        List<Float> vertices = new ArrayList<>();
        // faces or triangles indicate which vertices correspond to which face
        List<Integer> faces = new ArrayList<>();

        // typical file reading try catch block. Note that v stands for vertices, f stands for faces in obj
        // each line is a different vertex or face
        try (BufferedReader br = new BufferedReader(new FileReader(objFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("v ")) {
                    String[] tokens = line.split("\\s+"); // splits each line into array of strings
                    // input the xyz coords, takes the string and outputs floats
                    vertices.add(Float.parseFloat(tokens[1]));
                    vertices.add(Float.parseFloat(tokens[2]));
                    vertices.add(Float.parseFloat(tokens[3]));
                    // ignore the remaining because they are used for colors, JavaFX doesn't read vertex color
                } else if (line.startsWith("f ")) {
                    String[] tokens = line.split("\\s+"); // tokens[0-3]
                    for (int i = 1; i <= 3; i++) { // token[0] is f, ignored

                        int vertexIndex = Integer.parseInt(tokens[i]) - 1; // obj indexes from 1, JavaFX indexes from 0... honestly why
                        faces.add(vertexIndex);
                        faces.add(0); // JavaFX reads vertex texture, we have no texture, add 0
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
            return null;
        }

        // typically renderer needs array instead of array list so convert it
        float[] points = new float[vertices.size()];
        for (int i = 0; i < vertices.size(); i++) points[i] = vertices.get(i);
        mesh.getPoints().addAll(points);
        mesh.getTexCoords().addAll(0, 0); // dummy, u horizontal and v vertical axis of texture
        int[] facesArray = new int[faces.size()];
        for (int i = 0; i < faces.size(); i++) facesArray[i] = faces.get(i);
        mesh.getFaces().addAll(facesArray);

        MeshView meshView = new MeshView(mesh);
        meshView.setDrawMode(DrawMode.FILL);
        // meshView.setCullFace(CullFace.BACK); not needed because we have depth buffer true
        meshView.setMaterial(new PhongMaterial(Color.YELLOW));

        return meshView;

    }
}
// http://lagers.org.uk/javafx/usemeshview.html
// https://www.cs.cmu.edu/~mbz/personal/graphics/obj.html
// https://www.scratchapixel.com/lessons/3d-basic-rendering/obj-file-format/obj-file-format.html
