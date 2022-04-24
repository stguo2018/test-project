package com.ews.stguo.testproject.validate.vrbo.validator;

import com.ews.stguo.testproject.utils.file.RWFileUtils;
import com.ews.stguo.testproject.utils.controlfile.ControlFileRWUtils;
import com.ews.stguo.testproject.validate.vrbo.model.images.ImagesModel;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.EXPEDIAID;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.HCOMID;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.HEROHEIGHT;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.HEROLINK;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.HEROTITLE;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.HEROWIDTH;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.IMAGES;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.THUMBNAILHEIGHT;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.THUMBNAILLINK;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.THUMBNAILWIDTH;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.VRBOID;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class ImagesValidator extends VrboValidator<ImagesModel> {

    private Map<String, Pair<String, String>> vrboWebLikeMapping;

    public ImagesValidator(Set<Integer> hotelIds) {
        super(hotelIds);
        try {
            String urlTemplate = "https://www.vrbo.com/%s?adultsCount=2&arrival=2021-08-02&departure=2021-08-06&unitId=%s";
            List<String> stringList = ControlFileRWUtils.loadHotelIdStrByPaths(true, "E:/ews-29840/vrboIdMapping4.csv");
            vrboWebLikeMapping = stringList.stream().map(s -> s.split(","))
                    .collect(Collectors.toMap(ss -> ss[0], ss -> {
                        String vrboPropertyId = ss[2];
                        String vrboPropertyId2 = ss[1];
                        String[] columns = vrboPropertyId.split("\\.");
                        String listingId = columns[1];
                        String listingNum = columns[2];
                        String namespace = ss[3];
                        if ("trips".equals(namespace)) {
                            listingId = listingId + "ha";
                        } else if ("abritel".equals(namespace)) {
                            listingId = listingId + "a";
                        }
                        return Pair.of(vrboPropertyId2, String.format(urlTemplate, listingId, listingNum));
                    }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getFileType() {
        return "Images";
    }

    @Override
    public Class<ImagesModel> getClazz() {
        return ImagesModel.class;
    }

    @Override
    public void analyse(int index, ImagesModel model) {
        if (model.getPropertyId() != null) {
            if (StringUtils.isNotBlank(model.getPropertyId().getExpedia())) {
                hotelIds.remove(Integer.parseInt(model.getPropertyId().getExpedia()));
                updateCounter(EXPEDIAID);
            }
            if (StringUtils.isNotBlank(model.getPropertyId().getHcom())) {
                updateCounter(HCOMID);
            }
            if (StringUtils.isNotBlank(model.getPropertyId().getVrbo())) {
                updateCounter(VRBOID);
            }
        }

        if (model.getThumbnail() != null) {
            if (StringUtils.isNotBlank(model.getThumbnail().getWidth())) {
                updateCounter(THUMBNAILWIDTH);
            }
            if (StringUtils.isNotBlank(model.getThumbnail().getHeight())) {
                updateCounter(THUMBNAILHEIGHT);
            }
            if (StringUtils.isNotBlank(model.getThumbnail().getLink())) {
                updateCounter(THUMBNAILLINK);
            }
        }

        if (model.getHero() != null) {
            if (StringUtils.isNotBlank(model.getHero().getTitle())) {
                updateCounter(HEROTITLE);
            }
            if (StringUtils.isNotBlank(model.getHero().getWidth())) {
                updateCounter(HEROWIDTH);
            }
            if (StringUtils.isNotBlank(model.getHero().getHeight())) {
                updateCounter(HEROHEIGHT);
            }
            if (StringUtils.isNotBlank(model.getHero().getLink())) {
                updateCounter(HEROLINK);
            }
        }

        if (MapUtils.isNotEmpty(model.getImages())) {
            updateCounter(IMAGES);
        }

        if (vrboWebLikeMapping.containsKey(model.getPropertyId().getExpedia())) {
            try (BufferedWriter bw = RWFileUtils.getWriter("images.csv", true)) {
                String id = model.getPropertyId().getExpedia();
                Pair<String, String> pair = vrboWebLikeMapping.get(id);
                bw.write(String.format("%s,%s,%s", id, pair.getLeft(), pair.getRight()));
                bw.newLine();
                bw.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
