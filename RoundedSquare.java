import javafx.scene.shape.Rectangle;

public class RoundedSquare extends Rectangle {
    static final double DEFAULT_ARC_FRACTION = 0.325;
    static final int HALF_DIVISOR = 2;
    
    double arcFraction = DEFAULT_ARC_FRACTION;
    double centerX, centerY;

    //-----------------------------------------------------------------------
    //                           Constructors
    //-----------------------------------------------------------------------

    public RoundedSquare() {
        this(0, 0, 0);
    }
    
    public RoundedSquare(double size) {
        this(0, 0, size);
    }

    public RoundedSquare (double centerX, double centerY, double size) {
        this.centerX = centerX;
        this.centerY = centerY;
        setSize(size);
    }
    
    //-----------------------------------------------------------------------
    //                      Accessors for centerX
    //-----------------------------------------------------------------------
    
    public double getCenterX() {
        return centerX;
    }

    public void setCenterX(double centerX) {
        this.centerX = centerX;

        // Our superclass Rectangle is position by upper right, not by center
        this.setX(centerX - getSize() / HALF_DIVISOR);
    }
    
    //-----------------------------------------------------------------------
    //                      Accessors for centerY
    //-----------------------------------------------------------------------

    public double getCenterY() {
        return centerY;
    }

    public void setCenterY(double centerY) {
        this.centerY = centerY;

        // Our superclass Rectangle is position by upper right, not by center
        this.setY(centerY - getSize() / HALF_DIVISOR);
    }
    
    //-----------------------------------------------------------------------
    //                    Accessors for square size
    //-----------------------------------------------------------------------

    public double getSize() {
        return getWidth();
    }

    public void setSize(double size) {
        this.setWidth(size);
        this.setHeight(size);

        // Update super's arcWidth for the new size:
        double arcWidth = size * arcFraction;
        this.setArcWidth(arcWidth);
        this.setArcHeight(arcWidth);

        // Reposition to keep the center at centerX, centerY
        this.setX(centerX - size / HALF_DIVISOR);
        this.setY(centerY - size / HALF_DIVISOR);
    }
    
    //-----------------------------------------------------------------------
    //                    Accessors for arcFraction
    //-----------------------------------------------------------------------
    
    public double getArcFraction() {
        return arcFraction;
    }

    void setArcFraction(double arcFraction) {
        this.arcFraction = arcFraction;

        double arcWidth = this.getSize() * arcFraction;
        this.setArcWidth(arcWidth);
        this.setArcHeight(arcWidth);
    }
}