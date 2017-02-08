package org.kit.energy;

/**
 * Created by qa5147 on 08.02.2017.
 */
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.mapreduce.TableInputFormat;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.util.StringUtils;

public class TSDBInputFormat extends TableInputFormat implements Configurable {
    private final Log LOG = LogFactory.getLog(TSDBInputFormat.class);
    public static final String SCAN_TIMERANGE_START = "opentsdb.timerange.start";
    public static final String SCAN_TIMERANGE_END = "opentsdb.timerange.end";
    public static final String TSDB_UIDS = "net.opentsdb.tsdb.uid";
    public static final String METRICS = "net.opentsdb.rowkey";
    public static final String TAGKV = "net.opentsdb.tagkv";
    public static final String TSDB_STARTKEY = "net.opentsdb.start";
    public static final String TSDB_ENDKEY = "net.opentsdb.end";
    private Configuration conf = null;

    public TSDBInputFormat() {
    }

    public Configuration getConf() {
        return this.conf;
    }

    public static byte[] hexStringToByteArray(String s) {
        s = s.replace("\\x", "");
        byte[] b = new byte[s.length() / 2];

        for(int i = 0; i < b.length; ++i) {
            int index = i * 2;
            int v = Integer.parseInt(s.substring(index, index + 2), 16);
            b[i] = (byte)v;
        }

        return b;
    }

    public void setConf(Configuration configuration) {
        super.setConf(configuration);
        this.conf = configuration;
        Scan scan = null;

        try {
            scan = new Scan();
            String e;
            if(this.conf.get("net.opentsdb.tsdb.uid") != null) {
                e = String.format("^(%s)$", new Object[]{this.conf.get("net.opentsdb.tsdb.uid")});
                RegexStringComparator rowFilter = new RegexStringComparator(e);
                RowFilter keyRegEx = new RowFilter(CompareOp.EQUAL, rowFilter);
                scan.setFilter(keyRegEx);
            } else {
                if(this.conf.get("net.opentsdb.rowkey") != null) {
                    if(this.conf.get("net.opentsdb.tagkv") != null) {
                        e = String.format("^%s.+%s.*$", new Object[]{this.conf.get("net.opentsdb.rowkey"), this.conf.get("net.opentsdb.tagkv")});
                    } else {
                        e = String.format("^%s.+$", new Object[]{this.conf.get("net.opentsdb.rowkey")});
                    }

                    this.LOG.debug("Regex for row filtering: " + e);
                    Object rowFilter1;
                    if(this.conf.get("hbase.mapreduce.inputtable").contains("/")) {
                        this.LOG.info("DETECTED MAPR-DB TABLE. The rowFilter based on RegEx does not work as expected. Switching to PrefixFilter. Caution: All Tags of the metric will be returned.Make sure that the metric only contains one time series.");
                        rowFilter1 = new PrefixFilter(hexStringToByteArray(this.conf.get("net.opentsdb.rowkey")));
                    } else {
                        RegexStringComparator keyRegEx1 = new RegexStringComparator(e, 34);
                        keyRegEx1.setCharset(Charset.forName("ISO-8859-1"));
                        rowFilter1 = new RowFilter(CompareOp.EQUAL, keyRegEx1);
                    }

                    scan.setFilter((Filter)rowFilter1);
                }

                if(this.conf.get("opentsdb.timerange.start") != null) {
                    e = this.conf.get("net.opentsdb.rowkey") + this.conf.get("opentsdb.timerange.start") + (this.conf.get("net.opentsdb.tagkv") != null?this.conf.get("net.opentsdb.tagkv"):"");
                    scan.setStartRow(hexStringToByteArray(e));
                }

                if(this.conf.get("opentsdb.timerange.end") != null) {
                    e = this.conf.get("net.opentsdb.rowkey") + this.conf.get("opentsdb.timerange.end") + (this.conf.get("net.opentsdb.tagkv") != null?this.conf.get("net.opentsdb.tagkv"):"");
                    scan.setStopRow(hexStringToByteArray(e));
                }
            }

            scan.setCacheBlocks(this.conf.getBoolean("hbase.mapreduce.scan.cacheblocks", false));
        } catch (Exception var6) {
            this.LOG.error(StringUtils.stringifyException(var6));
        }

        this.setScan(scan);
    }

    protected void initialize(JobContext context) throws IOException {
        TableName tableName = TableName.valueOf(this.conf.get("hbase.mapreduce.inputtable"));

        try {
            this.initializeTable(ConnectionFactory.createConnection(new Configuration(this.conf)), tableName);
        } catch (Exception var4) {
            this.LOG.error(StringUtils.stringifyException(var4));
        }

    }
}
