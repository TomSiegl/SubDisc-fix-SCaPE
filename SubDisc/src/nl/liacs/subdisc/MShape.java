package nl.liacs.subdisc;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.Serializable;

import javax.swing.JComponent;
import javax.swing.JLabel;

public class MShape extends JComponent implements MouseMotionListener, MouseListener,
    ActionListener, Serializable
	{
		private static final long serialVersionUID = 1L;

		Color itsColor = Color.green;
		boolean setShadow = true;
        protected JLabel itsComponent = null;
        protected boolean dragging = false;
        protected Point dragpoint = new Point();

        /** Creates new Shape */
        public MShape(String label)
		{
            super();
            addMouseMotionListener(this);
            addMouseListener(this);
            itsComponent = new JLabel(label);
            //add(itsComponent);
            //itsComponent.addActionListener(this);
            //this.addActionListener(this);
        }

        public void repaint() { super.repaint(); }

        public Point getConnectPoint()
		{
            Rectangle r = this.getBounds();
            Point p = new Point((int)(r.x + (0.5 * r.width)), (int)(r.y + (0.5 * r.height)));
            return p;
        }

        public Dimension getPreferredSize()
		{
            Rectangle r = this.getBounds();
            return new Dimension(r.width, r.height);
        }

        /** The minimum size of the Shape. */
        public Dimension getMinimumSize()
		{
            Rectangle r = this.getBounds();
            return new Dimension(r.width, r.height);
        }

        protected boolean mouseOnMe(MouseEvent e) {
            Rectangle r = this.getBounds();
            if (e.getX() > r.x && r.x + r.width > e.getX())
                if ((e.getY() > r.y && r.y + r.height > e.getY()))
                    return true;
            return false;
        }

        public void mouseMoved(java.awt.event.MouseEvent mouseEvent) {
            if (mouseOnMe(mouseEvent));
        }

        public void mouseDragged(java.awt.event.MouseEvent mouseEvent) {
            if (mouseOnMe(mouseEvent) || dragging) {
                if (dragging) {
                    Rectangle r = this.getBounds();
                    setBounds(mouseEvent.getX() - dragpoint.x, mouseEvent.getY() - dragpoint.y, r.width, r.height);
                }
                else {
                    Rectangle r = this.getBounds();
                    dragpoint = new Point(mouseEvent.getX() - r.x, mouseEvent.getY() - r.y);
                    dragging = true;
                }
            }
        }

        public void mouseExited(java.awt.event.MouseEvent mouseEvent) {
            if (mouseOnMe(mouseEvent));
        }

        public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
            if (mouseOnMe(mouseEvent)) {
                dragging = false;
            }
        }

        public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
            if (mouseOnMe(mouseEvent));
        }

        public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
            if (mouseOnMe(mouseEvent));
        }

        public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {
            if (mouseOnMe(mouseEvent));
        }

        public void actionPerformed(java.awt.event.ActionEvent actionEvent) {

        }
}