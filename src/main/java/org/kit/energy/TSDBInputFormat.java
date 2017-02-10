package org.kit.energy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.mapreduce.TableInputFormat;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.util.StringUtils;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.regex.Pattern;
import org.apache.hadoop.hbase.filter.FilterBase;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.RowFilter;
/**
 * Convert HBase tabular data into a format that is consumable by Map/Reduce.
 */
public class TSDBInputFormat extends TableInputFormat implements Configurable {
    private final Log LOG = LogFactory.getLog(TSDBInputFormat.class);
    /**
     * The starting timestamp used to filter columns with a specific range of
     * versions.
     */
    public static final String SCAN_TIMERANGE_START = "opentsdb.timerange.start";
    /**
     * The ending timestamp used to filter columns with a specific range of
     * versions.
     */
    public static final String SCAN_TIMERANGE_END = "opentsdb.timerange.end";
    public static final String TSDB_UIDS = "net.opentsdb.tsdb.uid";
    /** The opentsdb metric to be retrived. */
    public static final String METRICS = "net.opentsdb.rowkey";
    /** The opentsdb metric to be retrived. */
    public static final String TAGKV = "net.opentsdb.tagkv";
    /** The tag keys for the associated metric (space seperated). */
    public static final String TSDB_STARTKEY = "net.opentsdb.start";
    /** The tag values for the tag keys (space seperated). */
    public static final String TSDB_ENDKEY = "net.opentsdb.end";
    /** The configuration. */
    private Configuration conf = null;
    /**
     * Returns the current configuration.
     *
     * @return The current configuration.
     * @see org.apache.hadoop.conf.Configurable#getConf()
     */
    @Override
    public Configuration getConf() {
        return conf;
    }
    public static byte[] hexStringToByteArray(String s) {
        s = s.replace("\\x", "");
        byte[] b = new byte[s.length() / 2];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(s.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }
    /**
     * Specific configuration for the OPENTSDB tables {tsdb, tsdb-uid}
     *
     * @param configuration
     *            The configuration to set.
     * @see org.apache.hadoop.conf.Configurable#setConf(org.apache.hadoop.conf.Configuration)
     */
    @Override
    public void setConf(Configuration configuration) {
        super.setConf(configuration);
        this.conf = configuration;
        Scan scan = null;
        try {
            scan = new Scan();
            // Configuration for extracting the UIDs for the user specified
            // metric and tag names.
            if (conf.get(TSDB_UIDS) != null) {
                // We get all uids for all specified column quantifiers
                // (metrics|tagk|tagv)
                String pattern = String.format("^(%s)$", conf.get(TSDB_UIDS));
                RegexStringComparator keyRegEx = new RegexStringComparator(pattern);
                RowFilter rowFilter = new RowFilter(CompareFilter.CompareOp.EQUAL, keyRegEx);
                scan.setFilter(rowFilter);
            } else {
                // Configuration for extracting & filtering the required rows
                // from tsdb table.
                if (conf.get(METRICS) != null) {
                    String name;
                    if (conf.get(TAGKV) != null) // If we have to extract based
                        // on a metric and its group
                        // of tags "^%s.{4}.*%s.*$"
                        name = String.format("^%s.+%s.*$", conf.get(METRICS), conf.get(TAGKV));
                    else
                        // If we have to extract based on just the metric
                        name = String.format("^%s.+$", conf.get(METRICS));
                    LOG.debug("Regex for row filtering: " + name);
                    FilterBase rowFilter;
                    if(conf.get(INPUT_TABLE).contains("/")) {
                        //Mapr-DB Workaround, regex comperator returns not all measurements
                        LOG.info("DETECTED MAPR-DB TABLE. The rowFilter based on RegEx does not work as expected. " +
                                "Switching to PrefixFilter. Caution: All Tags of the metric will be returned." +
                                "Make sure that the metric only contains one time series.");
                        rowFilter = new PrefixFilter(hexStringToByteArray(conf.get(METRICS)));
                    }
                    else {
                        RegexStringComparator keyRegEx = new RegexStringComparator(name, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                        keyRegEx.setCharset(Charset.forName("ISO-8859-1"));
                        rowFilter = new RowFilter(CompareFilter.CompareOp.EQUAL, keyRegEx);
                    }
                    scan.setFilter(rowFilter);
                }
                // Extracts data based on the supplied timerange. If timerange
                // is not provided then all data are extracted
                if (conf.get(SCAN_TIMERANGE_START) != null) {
                    String startRow = conf.get(METRICS) + conf.get(SCAN_TIMERANGE_START) + (conf.get(TAGKV) != null ? conf.get(TAGKV) : "");
                    scan.setStartRow(hexStringToByteArray(startRow));
                }
                if(conf.get(SCAN_TIMERANGE_END) != null) {
                    String endRow = conf.get(METRICS) + conf.get(SCAN_TIMERANGE_END) + (conf.get(TAGKV) != null ? conf.get(TAGKV) : "");
                    scan.setStopRow(hexStringToByteArray(endRow));
                }
            }
            // false by default, full table scans generate too much BC churn
            scan.setCacheBlocks((conf.getBoolean(SCAN_CACHEBLOCKS, false)));
        } catch (Exception e) {
            LOG.error(StringUtils.stringifyException(e));
        }
        setScan(scan);
    }
    @Override
    protected void initialize(JobContext context) throws IOException {
        // Do we have to worry about mis-matches between the Configuration from
        // setConf and the one
        // in this context?
        TableName tableName = TableName.valueOf(conf.get(INPUT_TABLE));
        try {
            initializeTable(
                    ConnectionFactory.createConnection(new Configuration(conf)),
                    tableName);
        } catch (Exception e) {
            LOG.error(StringUtils.stringifyException(e));
        }
    }
}
