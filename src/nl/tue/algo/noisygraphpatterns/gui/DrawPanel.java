package nl.tue.algo.noisygraphpatterns.gui;

import nl.tue.algo.noisygraphpatterns.data.graph.Vertex;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.PolyLine;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.geometrycore.geometryrendering.GeometryPanel;
import nl.tue.geometrycore.geometryrendering.styling.Dashing;
import nl.tue.geometrycore.geometryrendering.styling.ExtendedColors;
import nl.tue.geometrycore.geometryrendering.styling.Hashures;
import nl.tue.geometrycore.io.ipe.IPEWriter;

import java.io.File;
import java.io.IOException;

public class DrawPanel extends GeometryPanel {

    private final Data data;
    private final double scale = 16;
    private final double margin = 20;
    private final MatrixRendering matrix = new MatrixRendering(this.scale);
    private final NodeLinkRendering nodelink = new NodeLinkRendering(this.scale);
    private final BioFabricRendering biofabric = new BioFabricRendering(this.scale);

    public DrawPanel(Data data) {
        this.data = data;
    }

    private double dx() {
        double dx;
        if (data.drawDefault || data.drawRandom) {
            dx = scale * data.graph.vertices.length * 2.15 + 2 * margin;
        } else {
            dx = scale * data.graph.vertices.length * 1.10 + margin;
        }
        return dx;
    }

    @Override
    protected void drawScene() {
        if (data.graph == null) {
            return;
        }
        if (data.drawDefault) {
            matrix.render(this, data.orderings.get("Default"), 0, data.show_ordering, false, false, data.precisionBar, false);
            matrix.render(this, data.graph, scale * data.graph.vertices.length * 1.05 + margin, data.show_ordering,true, data.layout == null, data.precisionBar, !data.horizontalBar);
        } else if (data.drawRandom) {
            matrix.render(this, data.orderings.get("Random"), 0, data.show_ordering,false, false, data.precisionBar,false);
            matrix.render(this, data.graph, scale * data.graph.vertices.length * 1.05 + margin, data.show_ordering,true, data.layout == null, data.precisionBar, !data.horizontalBar);
        } else {
            matrix.render(this, data.graph, 0, data.show_ordering,true, data.layout == null, data.precisionBar, !data.horizontalBar);
        }
        if (data.layout != null) {
            if (data.show_rulers) {
                double l = scale * data.graph.vertices.length;
                double m = dx();
                for (Vertex v : data.graph.vertices) {
                    if (data.layout.get(v).position != null) {
                        double my = l - scale * (v.index + 0.5);
                        double ly = scale * data.layout.get(v).position.getY();
                        setStroke(ExtendedColors.lightGray, 0.4, Dashing.SOLID);
                        setFill(null, Hashures.SOLID);
                        draw(new PolyLine(new Vector(l, my), new Vector(m, ly), new Vector(m + scale * data.layout.get(v).position.getX(), ly)));
                    }
                }
            }

            nodelink.render(this, data.graph, data.layout, dx(), data.horizontalBar);

            if (data.layout.debug != null) {
                BaseGeometry bg = data.layout.debug.clone();
                bg.scale(scale);
                bg.translate(dx(), 0);
                setStroke(ExtendedColors.darkRed, 0.4, Dashing.SOLID);
                setFill(null, null);
                draw(bg);
            }

            if (data.show_biofabric) {
                biofabric.render(this, data.graph, 2 * dx());
                if (data.show_biomotifs) {
                    biofabric.renderMotif(this, data.graph, 2 * dx(), scale * data.graph.vertices.length * 1.05 + margin);
                }
            } else {
                if (data.show_biomotifs) {
                    biofabric.renderMotif(this, data.graph, 2 * dx(), 0);
                }
            }


        } else if (data.show_biofabric) {
            biofabric.render(this, data.graph, dx());
            if (data.show_biomotifs) {
                biofabric.renderMotif(this, data.graph, dx(), scale * data.graph.vertices.length * 1.05 + margin);
            }
        } else if (data.show_biomotifs) {
            biofabric.renderMotif(this, data.graph, dx(), 0);
        }
    }

    public void writeIPE(File f) {
        IPEWriter ipe = IPEWriter.fileWriter(f);
        ipe.setView(getBoundingRectangle());
        try {
            ipe.initialize();
        } catch (IOException e) {
            System.err.println("Error initializing IPE");
        }

        if (data.graph == null) {
            return;
        }

        if (data.drawDefault) {
            matrix.render(ipe, data.orderings.get("Default"), 0, data.show_ordering,false, false, data.precisionBar,false);
            matrix.render(ipe, data.graph, scale * data.graph.vertices.length * 1.05 + margin, data.show_ordering,true, data.layout == null, data.precisionBar, !data.horizontalBar);
        } else if (data.drawRandom) {
            matrix.render(ipe, data.orderings.get("Random"), 0, data.show_ordering,false, false, data.precisionBar, false);
            matrix.render(ipe, data.graph, scale * data.graph.vertices.length * 1.05 + margin, data.show_ordering,true, data.layout == null, data.precisionBar, !data.horizontalBar);
        } else {
            matrix.render(ipe, data.graph, 0, data.show_ordering,true, data.layout == null, data.precisionBar, !data.horizontalBar);
        }

        if (data.layout != null) {
            nodelink.render(ipe, data.graph, data.layout, dx(), data.horizontalBar);

            if (data.show_biofabric) {
                biofabric.render(ipe, data.graph, 2 * dx());
                if (data.show_biomotifs) {
                    biofabric.renderMotif(ipe, data.graph, 2 * dx(), scale * data.graph.vertices.length * 1.05 + margin);
                }
            } else {
                if (data.show_biomotifs) {
                    biofabric.renderMotif(ipe, data.graph, 2 * dx(), 0);
                }
            }

        } else if (data.show_biofabric) {
            biofabric.render(ipe, data.graph, dx());
            if (data.show_biomotifs) {
                biofabric.renderMotif(ipe, data.graph, dx(), scale * data.graph.vertices.length * 1.05 + margin);
            }
        } else if (data.show_biomotifs) {
            biofabric.renderMotif(ipe, data.graph, dx(), 0);
        }

        try {
            ipe.closeWithResult();
        } catch (IOException e) {
            System.err.println("Error closing IPE");
        }
    }

    @Override
    public Rectangle getBoundingRectangle() {
        Rectangle R = Rectangle.byCornerAndSize(new Vector(-scale * data.graph.vertices.length * 0.05, -scale * data.graph.vertices.length * 0.05),
                                                        dx() + scale * data.graph.vertices.length * 0.05, scale * data.graph.vertices.length * 1.05);
        R.setRight(R.getRight() + margin);
        R.setLeft(R.getLeft() - margin);
        R.setTop(R.getTop() + margin);
        R.setBottom(R.getBottom() - margin);
        if (data.drawDefault || data.drawRandom) {
            R.setRight(R.getRight() + scale * dx());
        }
        if (data.layout != null) {
            R.setRight(R.getRight() + dx());
            R.setTop(Math.max(R.getTop(), scale * data.layout.height() - data.layout.dy()) + 2 * margin);
        }
        if(data.show_biofabric || data.show_biomotifs) {
            R.setRight(R.getRight() + scale * (data.graph.edgeCount() + data.graph.vertices.length * 0.05) + margin);
            if (data.show_biofabric && data.show_biomotifs) {
                R.setTop(R.getTop() + scale * data.graph.vertices.length * 1.10 + margin);
            }
        }
        return R;
    }

    @Override
    protected void mousePress(Vector loc, int button, boolean ctrl, boolean shift, boolean alt) {

    }

    @Override
    protected void keyPress(int keycode, boolean ctrl, boolean shift, boolean alt) {

    }
}
