/******************************************************************************
 * Copyright 2017 Steven Foskett, Jimmy Ho, Ryan Porterfield                  *
 * Permission is hereby granted, free of charge, to any person obtaining a    *
 * copy of this software and associated documentation files (the "Software"), *
 * to deal in the Software without restriction, including without limitation  *
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,   *
 * and/or sell copies of the Software, and to permit persons to whom the      *
 * Software is furnished to do so, subject to the following conditions:       *
 *                                                                            *
 * The above copyright notice and this permission notice shall be included in *
 * all copies or substantial portions of the Software.                        *
 *                                                                            *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,   *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE*
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER     *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING    *
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER        *
 * DEALINGS IN THE SOFTWARE.                                                  *
 ******************************************************************************/

package com.einzig.ipst2.database;

import android.database.Cursor;

import com.einzig.ipst2.portal.PortalSubmission;
import com.einzig.ipst2.util.Logger;

import org.joda.time.LocalDate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.einzig.ipst2.database.DatabaseInterface.DATE_FORMATTER;

/**
 * @author Ryan Porterfield
 * @since 2017-05-19
 */
public abstract class PortalBuilder<P extends PortalSubmission> {

    /**
     *
     */
    PortalBuilder() {
    }

    /**
     * Create an instance of Portal* from a database entry.
     *
     * @param cursor Cursor containing the database fields of the portal.
     * @return instance of Portal* from a database entry.
     */
    abstract P build(Cursor cursor);

    /**
     * Build a portal from a CSV file
     *
     * @param csvLine Line of the CSV file portal is being built from
     * @return portal from a CSV file
     */
    abstract P build(String[] csvLine);

    /**
     * Create a new portal
     *
     * @param name          The portal name.
     * @param dateResponded The date the portal was rejected.
     * @param message       The body of the email as a String for parsing.
     */
    public abstract P build(String name, LocalDate dateResponded, String message);

    static public PortalSubmission buildFromCSV(String[] csvLine) {
        String status = csvLine[4];
        Logger.d("Importing Status is: " + status);
        if (status.equalsIgnoreCase("Accepted"))
            return new PortalAcceptedBuilder().build(csvLine);
        else if (status.equalsIgnoreCase("Rejected"))
            return new PortalRejectedBuilder().build(csvLine);
        else
            return new PortalSubmissionBuilder().build(csvLine);
    }

    LocalDate parseDate(String dateString) {
        try {
            return DATE_FORMATTER.parseLocalDate(dateString);
        } catch (IllegalArgumentException e) {
            return LocalDate.now();
        }
    }

    /**
     * Parse the URL of the portal picture from the email.
     *
     * @param message The body of the email as a String for parsing.
     * @return the URL of the portal picture.
     */
    String parsePictureURL(String message, String portalName) {
        String pictureURL = portalName;
        Pattern pattern = Pattern.compile("<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        try {
            Matcher matcher = pattern.matcher(message);
            if (matcher.find())
                pictureURL = matcher.group(1);
        } catch (Exception e) {
            e.printStackTrace();
            pictureURL = "";
        }
        return pictureURL;
    }
}
