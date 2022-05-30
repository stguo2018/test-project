SET hive.compute.splits.in.am=false;
SELECT hr1.bt, count(*) FROM (
                                 SELECT hr2.bt,
                                        hr2.h,
                                        count(*) as size
                                 FROM
                                     (
                                     SELECT
                                     hr3.bt,
                                   hr3.h
                                         , CASE
                                     WHEN unix_timestamp( hr3.sd ) IS NOT NULL THEN from_unixtime( unix_timestamp( hr3.sd ), 'yyyy-MM-dd HH:mm:ss' )
                                     ELSE from_unixtime( unix_timestamp( hr3.sd, 'MMM d, yyyy h:m:s a' ), 'yyyy-MM-dd HH:mm:ss' )
                                     END AS sd
                                         , row_number() over (PARTITION BY hr3.review_id ORDER BY hr3.lud DESC) AS review_num
                                     FROM
                                     lz.lz_urs_hotel_review hr3
                                     WHERE
                                     hr3.h IN (%s)
                                     AND hr3.l LIKE '%s'
                                     AND (
                                     hr3.bt = 'HomeAway'
                                     OR hr3.bt = 'Expedia'
                                     OR hr3.bt = 'Hotels'
                                     )
                                     AND hr3.r IS NOT NULL
                                     AND hr3.r <> ''
                                     AND hr3.s = 'APPROVED'
                                     ) hr2
                                 WHERE
                                     hr2.review_num = 1
                                 GROUP BY hr2.bt, hr2.h
                             ) hr1 WHERE hr1.size >= 500 group by hr1.bt
;
