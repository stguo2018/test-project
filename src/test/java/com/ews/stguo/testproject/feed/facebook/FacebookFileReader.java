package com.ews.stguo.testproject.feed.facebook;

import com.ews.stguo.testproject.utils.file.RWFileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class FacebookFileReader implements Closeable {

    private BufferedReader br;

    private FacebookFileReader() {
    }

    public static FacebookFileReader readFile(String path) throws IOException {
        FacebookFileReader fr = new FacebookFileReader();
        fr.br = RWFileUtils.getReader(path);
        return fr;
    }

    public String readLine() throws IOException {
        return String.join("\t", readLineAsColumns());
    }

    public String[] readLineAsColumns() throws IOException {
        String line;
        StringBuilder contentLine = new StringBuilder();
        while ((line = br.readLine()) != null) {
            contentLine.append(line.replace("\\", "").replace("\t\"\",\"\"postal_code\"\"", "\"\",\"\"postal_code\"\""));
            String[] columns = contentLine.toString().trim().split("\t");
            if (columns.length < 16) {
                continue;
            } else if (columns.length > 16) {
                try {
                    while (!columns[1].startsWith("\"{\"\"iphone_url\"\":") && columns.length > 16) {
                        String[] newColumns = new String[columns.length - 1];
                        int newIndex = 0;
                        for (int i = 0; i < columns.length; i++) {
                            newColumns[newIndex] = columns[i];
                            if (i == 0) {
                                newColumns[newIndex] += columns[i + 1];
                                i++;
                            }
                            newIndex++;
                        }
                        columns = newColumns;
                    }
                    while (((columns[11].equals("") && StringUtils.isNumeric(columns[12])) || !StringUtils.isNumeric(columns[11]))
                            && columns.length > 16) {
                        String[] newColumns = new String[columns.length - 1];
                        int newIndex = 0;
                        for (int i = 0; i < columns.length; i++) {
                            newColumns[newIndex] = columns[i];
                            if (i == (11 - 1)) {
                                newColumns[newIndex] += columns[i + 1];
                                i++;
                            }
                            newIndex++;
                        }
                        columns = newColumns;
                    }
                } catch (Exception e) {
                    System.out.println("-----------------ERROR:" + contentLine);
                }
            }
            if (columns.length != 16) {
                System.out.println(contentLine);
            }
            return columns;
        }
        return new String[0];
    }

    public DataLine readDataLine() throws IOException {
        String[] columns = readLineAsColumns();
        if (columns.length == 0) {
            return null;
        }
        DataLine dataLine = new DataLine();
        dataLine.setAddress(columns[0]);
        dataLine.setApplink(columns[1]);
        dataLine.setBasePrice(columns[2]);
        dataLine.setBrand(columns[3]);
        dataLine.setDescription(columns[4]);
        dataLine.setGuestRatings(columns[5]);
        dataLine.setHotelId(columns[6]);
        dataLine.setImage(columns[7]);
        dataLine.setLatitude(columns[8]);
        dataLine.setLongitude(columns[9]);
        dataLine.setName(columns[10]);
        dataLine.setStarRating(columns[11]);
        dataLine.setUrl(columns[12]);
        dataLine.setNeighborhood(columns[13]);
        dataLine.setCategory(columns[14]);
        dataLine.setCustomLabel0(columns[15]);
        return dataLine;
    }

    @Override
    public void close() throws IOException {
        if (br != null) {
            br.close();
        }
    }

    public static class DataLine {
        private String address;
        private String applink;
        private String basePrice;
        private String brand;
        private String description;
        private String guestRatings;
        private String hotelId;
        private String image;
        private String latitude;
        private String longitude;
        private String name;
        private String starRating;
        private String url;
        private String neighborhood;
        private String category;
        private String customLabel0;

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getApplink() {
            return applink;
        }

        public void setApplink(String applink) {
            this.applink = applink;
        }

        public String getBasePrice() {
            return basePrice;
        }

        public void setBasePrice(String basePrice) {
            this.basePrice = basePrice;
        }

        public String getBrand() {
            return brand;
        }

        public void setBrand(String brand) {
            this.brand = brand;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getGuestRatings() {
            return guestRatings;
        }

        public void setGuestRatings(String guestRatings) {
            this.guestRatings = guestRatings;
        }

        public String getHotelId() {
            return hotelId;
        }

        public void setHotelId(String hotelId) {
            this.hotelId = hotelId;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getLatitude() {
            return latitude;
        }

        public void setLatitude(String latitude) {
            this.latitude = latitude;
        }

        public String getLongitude() {
            return longitude;
        }

        public void setLongitude(String longitude) {
            this.longitude = longitude;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getStarRating() {
            return starRating;
        }

        public void setStarRating(String starRating) {
            this.starRating = starRating;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getNeighborhood() {
            return neighborhood;
        }

        public void setNeighborhood(String neighborhood) {
            this.neighborhood = neighborhood;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getCustomLabel0() {
            return customLabel0;
        }

        public void setCustomLabel0(String customLabel0) {
            this.customLabel0 = customLabel0;
        }
    }

}
