SET hive.compute.splits.in.am=false;
SELECT
    sub_hotel_review.review_id
     ,sub_hotel_review.h
     ,sub_hotel_review.l
     ,sub_hotel_review.ro
     ,sub_hotel_review.r
     ,sub_hotel_review.sd
     ,sub_hotel_review.nn
     ,sub_hotel_review.cidt
     ,sub_hotel_review.codt
     ,sub_hotel_review.dllangcode
     ,row_number() over (PARTITION BY sub_hotel_review.h ORDER BY sub_hotel_review.sd DESC) AS rank_code
FROM
    (
        SELECT
            review_id
             ,h
             ,l
             ,CASE
                  WHEN bt = 'Hotels' THEN ro * 2
                  ELSE ro
            END AS ro
             ,r
             ,CASE
                  WHEN unix_timestamp( sd ) IS NOT NULL THEN from_unixtime( unix_timestamp( sd ) ,'yyyy-MM-dd HH:mm:ss' )
                  ELSE from_unixtime( unix_timestamp( sd ,'MMM d, yyyy h:m:s a' ) ,'yyyy-MM-dd HH:mm:ss' )
            END AS sd
             ,row_number() over (PARTITION BY review_id ORDER BY lud DESC) AS review_update_order
					,nn
             ,cidt
             ,codt
             ,dllangcode
        FROM
            lz.lz_urs_hotel_review
        WHERE
                h IN (%s)
          AND l LIKE '%s'
          AND bt = '%s'
          AND r IS NOT NULL
          AND r <> ''
          AND s = 'APPROVED'
          AND (
                    bt = 'Hotels'
                OR
                    (
                            (unix_timestamp(sd) IS NULL AND from_unixtime(unix_timestamp(sd ,'MMM d, yyyy h:m:s a') ,'yyyy-MM-dd HH:mm:ss') < date_sub (CURRENT_TIMESTAMP(),90))
                            OR
                            (unix_timestamp(sd) IS NOT NULL AND from_unixtime(unix_timestamp(sd) ,'yyyy-MM-dd HH:mm:ss') < date_sub (CURRENT_TIMESTAMP(),90))
                        )
            )
          AND (
                (unix_timestamp(sd) IS NULL AND from_unixtime(unix_timestamp(sd ,'MMM d, yyyy h:m:s a') ,'yyyy-MM-dd HH:mm:ss') > date_sub (CURRENT_TIMESTAMP(),730))
                OR
                (unix_timestamp(sd) IS NOT NULL AND from_unixtime(unix_timestamp(sd) ,'yyyy-MM-dd HH:mm:ss') > date_sub (CURRENT_TIMESTAMP(),730))
            )
    ) sub_hotel_review
WHERE
        sub_hotel_review.review_update_order = 1

;