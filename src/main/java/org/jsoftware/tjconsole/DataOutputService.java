package org.jsoftware.tjconsole;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class DataOutputService {
    private static final DataOutputService defaultDataOutputService = new ToStringDataOutputService();
    private static final Map<String, DataOutputService> dataOutputServices;

    static {
        dataOutputServices = new HashMap<String, DataOutputService>();
        dataOutputServices.put(CompositeDataSupport.class.getName(), new CompositeDataOutputService());
        dataOutputServices.put(CompositeData.class.getName(), new CompositeDataOutputService());
        dataOutputServices.put(TabularDataSupport.class.getName(), new TabularDataOutputService());
        dataOutputServices.put(TabularData.class.getName(), new TabularDataOutputService());
        dataOutputServices.put("void", new VoidDataOutputService());
        dataOutputServices.put(Date.class.getName(), new DateDataOutputService());
    }


    public static DataOutputService get(String type) {
        DataOutputService dos = dataOutputServices.get(type);
        if (type.startsWith("[L")) {
            dos = new ArrayDataOutputService();
        }
        if (dos == null) {
            dos = defaultDataOutputService;
        }
        return dos;
    }

    public abstract void output(Object data, TJContext tjContext, Appendable output) throws IOException;

}


class ToStringDataOutputService extends DataOutputService {
    @Override
    public void output(Object data, TJContext tjContext, Appendable output) throws IOException {
        if (data == null) {
            output.append("null");
        } else if ("".equals(data.toString())) {
            output.append("<empty string>");
        } else {
            output.append(data.toString());
        }
    }

}

class TabularDataOutputService extends DataOutputService {

    @Override
    public void output(Object data, TJContext tjContext, Appendable output) throws IOException {
        TabularData td = (TabularData) data;
        output.append("TabularData: of ").append(td.getTabularType().toString()).append("{\n");
        int index = 0;
        for (Object o : td.values()) {
            output.append('[').append(Integer.toString(index)).append("]: ");
            DataOutputService.get(o.getClass().getName()).output(o, tjContext, output);
            output.append('\n');
        }
        output.append("}\n");
    }
}

class CompositeDataOutputService extends DataOutputService {

    @Override
    public void output(Object data, TJContext tjContext, Appendable output) throws IOException {
        CompositeData cd = (CompositeData) data;
        output.append("CompositeData:").append(cd.getCompositeType().getTypeName()).append("{\n");
        for (Object o : cd.values()) {
            DataOutputService.get(o.getClass().getName()).output(o, tjContext, output);
            output.append('\n');
        }
        output.append("}\n");
    }
}

class VoidDataOutputService extends DataOutputService {

    @Override
    public void output(Object data, TJContext tjContext, Appendable output) throws IOException {
    }
}

class ArrayDataOutputService extends DataOutputService {

    @Override
    public void output(Object data, TJContext tjContext, Appendable output) throws IOException {
        output.append("Array[\n");
        for (int i = 0; i < Array.getLength(data); i++) {
            Object o = Array.get(data, i);
            output.append("index:").append(String.valueOf(i)).append(" = ");
            get(o.getClass().getName()).output(o, tjContext, output);
            output.append('\n');
        }
        output.append("]\n");
    }
}

class DateDataOutputService extends DataOutputService {

    @Override
    public void output(Object data, TJContext tjContext, Appendable output) throws IOException {
        if (data == null) {
            output.append("null");
        } else {
            String dateFormat = (String) tjContext.getEnvironment().get("DATE_FORMAT");
            SimpleDateFormat sdf;
            if (dateFormat != null && dateFormat.trim().length() > 0) {
                sdf = new SimpleDateFormat(dateFormat);
            } else {
                sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            }
            output.append(sdf.format((Date) data));
        }
    }
}