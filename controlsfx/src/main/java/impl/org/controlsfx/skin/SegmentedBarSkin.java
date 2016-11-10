/**
 * Copyright (c) 2016, ControlsFX
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package impl.org.controlsfx.skin;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.util.Callback;
import org.controlsfx.control.SegmentedBar;

import java.util.*;

public class SegmentedBarSkin<T extends SegmentedBar.Segment> extends SkinBase<SegmentedBar<T>> {

    private Map<T, Node> segmentNodes = new HashMap<>();

    private InvalidationListener buildListener = it -> buildSegments();

    private WeakInvalidationListener weakBuildListener = new WeakInvalidationListener(buildListener);

    public SegmentedBarSkin(SegmentedBar bar) {
        super(bar);

        bar.segmentViewFactoryProperty().addListener(weakBuildListener);
        bar.getSegments().addListener(weakBuildListener);

        buildSegments();
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (getSkinnable().getOrientation().equals(Orientation.HORIZONTAL)) {
            OptionalDouble maxHeight = getChildren().stream().mapToDouble(node -> node.prefHeight(-1)).max();
            if (maxHeight.isPresent()) {
                return maxHeight.getAsDouble();
            }
        }

        return 0;
    }

    @Override
    protected double computePrefWidth(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (getSkinnable().getOrientation().equals(Orientation.VERTICAL)) {
            OptionalDouble maxWidth = getChildren().stream().mapToDouble(node -> node.prefWidth(-1)).max();
            if (maxWidth.isPresent()) {
                return maxWidth.getAsDouble();
            }
        }

        return 0;
    }

    @Override
    protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (getSkinnable().getOrientation().equals(Orientation.HORIZONTAL)) {
            return computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
        }

        return 0;
    }

    @Override
    protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (getSkinnable().getOrientation().equals(Orientation.VERTICAL)) {
            return computePrefWidth(height, topInset, rightInset, bottomInset, leftInset);
        }

        return 0;
    }

    @Override
    protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (getSkinnable().getOrientation().equals(Orientation.HORIZONTAL)) {
            return computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
        }

        return Double.MAX_VALUE;
    }

    @Override
    protected double computeMaxWidth(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (getSkinnable().getOrientation().equals(Orientation.VERTICAL)) {
            return computePrefWidth(width, topInset, rightInset, bottomInset, leftInset);
        }

        return Double.MAX_VALUE;
    }

    private void buildSegments() {
        segmentNodes.clear();
        getChildren().clear();

        List<T> segments = getSkinnable().getSegments();
        int size = segments.size();

        Callback<T, Node> cellFactory = getSkinnable().getSegmentViewFactory();

        for (int i = 0; i < size; i++) {
            T segment = segments.get(i);
            Node segmentNode = cellFactory.call(segment);
            segmentNodes.put(segment, segmentNode);
            getChildren().add(segmentNode);

            segmentNode.getStyleClass().add("segment");

            if (i == 0) {
                if (size == 1) {
                    segmentNode.getStyleClass().add("only-segment");
                } else {
                    segmentNode.getStyleClass().add("first-segment");
                }
            } else if (i == size - 1) {
                segmentNode.getStyleClass().add("last-segment");
            } else {
                segmentNode.getStyleClass().add("middle-segment");
            }
        }

        getSkinnable().requestLayout();
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        double total = getSkinnable().getTotal();

        List<T> segments = getSkinnable().getSegments();
        int size = segments.size();

        double x = contentX;
        double y = contentY + contentHeight;

        for (int i = 0; i < size; i++) {
            SegmentedBar.Segment segment = segments.get(i);
            Node segmentNode = segmentNodes.get(segment);
            double segmentValue = segment.getValue();

            if (getSkinnable().getOrientation().equals(Orientation.HORIZONTAL)) {
                double segmentWidth = segmentValue / total * contentWidth;
                segmentNode.resizeRelocate(x, contentY, segmentWidth, contentHeight);
                x += segmentWidth;
            } else {
                double segmentHeight = segmentValue / total * contentHeight;
                segmentNode.resizeRelocate(contentX, y - segmentHeight, contentWidth, segmentHeight);
                y -= segmentHeight;
            }
        }
    }
}
