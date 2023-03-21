import java.io.IOException;
import java.util.*;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class Unhealthy_relationship {
    
    public static class NodeMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
        
        private Text node = new Text();
        private final static IntWritable one = new IntWritable(1);
        private final static IntWritable negate_one = new IntWritable(-1);
        
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String line = value.toString();
            String[] tokens = line.split(" ");
            node.set(tokens[0]);
            context.write(node, one);
            node.set(tokens[1]);
            context.write(node, negate_one);
        }
    }
    
    public static class NodeReducer extends Reducer<Text, IntWritable, Text, Text> {
        
        private Text result = new Text();
        
        public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
            int delta = 0;
            int gamma = 0;
            for (IntWritable val : values) {
                if (val.get() > 0) {
                    delta++;
                } else {
                    gamma++;
                }
            }
            int score = delta - gamma;
            String label = "";
            if (score > 0) {
                label = "pos";
            } else if (score == 0) {
                label = "eq";
            } else {
                label = "neg";
            }
            result.set(label);
            context.write(key, result);
        }
    }
    
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
        if (otherArgs.length != 2) {
            System.err.println("Usage: NodeLabeler <in> <out>");
            System.exit(2);
        }
        Job job = Job.getInstance(conf, "node labeler");
        job.setJarByClass(Unhealthy_relationship.class);
        job.setMapperClass(NodeMapper.class);
        job.setReducerClass(NodeReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
        FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}

