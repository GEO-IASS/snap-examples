/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package org.acme.snap.examples.data_export;

import org.esa.snap.core.dataio.ProductIO;
import org.esa.snap.core.datamodel.GeoCoding;
import org.esa.snap.core.datamodel.GeoPos;
import org.esa.snap.core.datamodel.PixelPos;
import org.esa.snap.core.datamodel.Product;

import java.io.FileWriter;
import java.io.IOException;

/**
 * This example program extracts the geolocation information of a satellite product to an envi compatible
 * ground control points file.
 *
 * The program expects three program arguments
 * <ul>
 * <li>the gcp file to be written</li>
 * <li>the satellite product to be opened</li>
 * <li>the resolution of the GCPs, i.e. how many pixels inbetween two GCPs</li>
 * </ul>
 */
public class GcpExportMain {

    private static final String _GCP_LINE_SEPARATOR = System.getProperty("line.separator");

    public static void main(String[] args) {

        FileWriter writer = null;

        try {
            writer = new FileWriter(args[0]);

            // open product and extract the geocoding
            Product product = ProductIO.readProduct(args[1]);
            GeoCoding geoCoding = product.getSceneGeoCoding();


            final int width = product.getSceneRasterWidth();
            final int height = product.getSceneRasterHeight();
            final int resolution = Integer.parseInt(args[2]);
            final int gcpWidth = Math.max(width / resolution + 1, 2); //2 minimum
            final int gcpHeight = Math.max(height / resolution + 1, 2);//2 minimum
            final double xMultiplier = 1f * (width - 1) / (gcpWidth - 1);
            final double yMultiplier = 1f * (height - 1) / (gcpHeight - 1);
            final PixelPos pixelPos = new PixelPos();
            final GeoPos geoPos = new GeoPos();

            writer.write(createLineString("; ENVI Registration GCP File"));
            for (int y = 0; y < gcpHeight; y++) {
                for (int x = 0; x < gcpWidth; x++) {
                    final double imageX = xMultiplier * x;
                    final double imageY = yMultiplier * y;
                    pixelPos.x = imageX + 0.5f;
                    pixelPos.y = imageY + 0.5f;
                    geoCoding.getGeoPos(pixelPos, geoPos);
                    final double mapX = geoPos.lon; //longitude
                    final double mapY = geoPos.lat; //latitude
                    writer.write(createLineString(mapX, mapY,
                            pixelPos.x + 1, // + 1 because ENVI uses a one-based pixel co-ordinate system
                            pixelPos.y + 1));
                }
            }
            writer.close();
            writer = null;
        } catch (IOException e) {
            e.printStackTrace();  
        }
    }

    private static String createLineString(final String str) {
        return str.concat(_GCP_LINE_SEPARATOR);
    }

    private static String createLineString(final double mapX, final double mapY, final double imageX, final double imageY) {
        return "" + mapX + "\t" + mapY + "\t" + imageX + "\t" + imageY + _GCP_LINE_SEPARATOR;
    }
}
