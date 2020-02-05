package com.bitpops.barcode.Model;

public class Transfer {

    public String from = "";
    public String to = "";
    public String sales_person_id = "";
    public String sales_person_name = "";


    public Transfer(String _from, String _to, String _sales_person_id, String _sales_person_name)
    {
        from = _from;
        to = _to;
        sales_person_id = _sales_person_id;
        sales_person_name = _sales_person_name;
    }
}
