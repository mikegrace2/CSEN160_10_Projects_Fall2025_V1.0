package org.example;

public interface CharMatrix3D {
    void generate();

    // Visualization methods
    void printSlice(int z);
    void printSliceX(int x);
    void printSliceY(int y);
    void visualize();
    void visualizeCrossSections();
}