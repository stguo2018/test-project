SELECT eg_property_id              AS bexId,
       hcom_property_id            AS hcomId,
       biddable.google.bexg.status AS bexBiddable,
       biddable.google.hcom.status AS hcomBiddable,
       sellable.bexg.status        AS bexSellable,
       sellable.hcom.status        AS hcomSellable
FROM hcom_data_prod_bid_hwwsam.eg_unified_meta_inventory
WHERE update_datetm = (select latest_update_datetm
                       FROM hcom_data_prod_bid_hwwsam.vulcan_table_latest_update_log
                       WHERE table_name = 'EGUMI')
  AND eg_property_id IN (%s);