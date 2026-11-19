package com.fs.starfarer.api.ui;

import java.util.List;

import java.awt.Color;

import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.StatBonus;

/**
 * x, y are cell coordinates, not screen coordinates. "Row" in this interface really means "cell", not a row of cells.
 * 
 * @author Alex
 *
 */
public interface DataGridAPI extends UIPanelAPI {

    public interface DataGridRowAPI extends UIPanelAPI {
        LabelAPI getNameLabel();
        LabelAPI getValueLabel();
    }

    String getValueFont();
    void setValueFont(String font);
    String getNameFont();
    void setNameFont(String font);

    /**
     * Put the value on the left, right justified in its space valueWidth pixels wide, with gap pixels to the right,
     * and the label to the right of the gap. Applies to all rows/cells.
     * @param valueWidth
     * @param gap
     */
    void setFlipped(float valueWidth, float gap);

    float computeHeight(int rowCount);
    void autoSizeToRows(int rowCount);

    float getItemWidth();
    float getUnitsWidth();
    float getRowHeight();

    DataGridRowAPI updateRowText(int x, int y, String text);
    DataGridRowAPI getRow(int x, int y);

    float getHpad();
    void setHpad(float pad);

    /**
     * Number of rendering passes for fonts. 
     * @param count
     */
    void setNumAA(int count);
    
    /**
     * Spacing between rendering passes for fonts.
     * @param increment
     */
    void setAaIncr(float increment);
    void setFontSize(float size);

    void setFontConfigForBarFont();
    void setFontConfigForSmallInsignia();

    void setNameColor(Color color);
    void setValueColor(Color color);
    void setUnitsColor(Color color);

    DataGridRowAPI add(int x, int y, String nameText, String valueText);
    DataGridRowAPI add(int x, int y, String nameText, String valueText, String unitText);
    DataGridRowAPI add(int x, int y, int w, int h, String nameText, String valueText, String unitText);
    DataGridRowAPI addOrUpdate(int x, int y, String nameText, String valueText);
    DataGridRowAPI addOrUpdate(int x, int y, int w, int h, String nameText, String valueText);
    DataGridRowAPI add(int x, int y, String nameText, StatBonus stat, float baseValue);
    DataGridRowAPI add(int x, int y, String nameText, StatBonus stat, float baseValue, boolean decimalFormatting, boolean showPercent, boolean invertColor);
    DataGridRowAPI add(int x, int y, String nameText, String valueText, Color valueColor);
    DataGridRowAPI add(int x, int y, String nameText, String valueText, MutableStat stat, boolean showPercent, boolean invertColor);

    void removeRow(int x, int y);
    void clearRows();

    int getRowCount();
    List<DataGridRowAPI> getRows();

    void updateColor(int x, int y, Color valueColor);
}
