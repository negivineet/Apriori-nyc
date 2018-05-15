// Statup options: Run with "-Xmx100m" to limit heap size to 100MB.
// e.g. java -Xmx100m AP < 311-s > out.txt

import java.util.Iterator;
import java.util.Scanner;
import java.util.NoSuchElementException;
import java.util.Date;
import java.util.HashMap;
import java.util.Calendar;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class AP {
    
    static boolean debug = false;
    static boolean verbose = false;
    
    public static void main(String[] args) {

        AP ap = new AP(args);
        
    }
    
    public void debugPrint(String message) {
        if (debug) {
            System.out.println(message);
        }
    }

    public AP (String[] args) {
        
        if (args.length < 1) {
            System.out.println("No arguments. Entering test mode.");
            
//            testPrint();
//            testMapAndTree();

            System.exit(0);
        }

        // mode should be "-g", "-v", or "-m" based on the assignment
        String mode = args[0];
        
        switch (mode) {
            case "-g":  generate();
                break;
            case "-v":  verbose(args);
                break;
            case "-m":  master(args);
                break;
            default:    System.out.println("Unrecognized mode. Exiting...");
                System.exit(0);
        }

    }
        
    public void generate() {
        
        // Vineet's code here
        String line = "";
		String newCol = "";
		Scanner newcolScanner = new Scanner(System.in);
		String[] newColArray = null;
		while (newcolScanner.hasNext()) {
			// get the next line
			line = newcolScanner.nextLine();
			newColArray = parseLine(line, 46);
			if (newColArray[1].equals("Created Date")) {
				newCol = "New_Column";
			} else {
                try {
                    newCol = extraColumn(newColArray[1]);
                } catch (ParseException e) {
				    e.printStackTrace();
                }
			}
			System.out.println(newColArray[0] + "," + newCol);
		}
		newcolScanner.close();
    }
    
    public static String extraColumn(String a) throws ParseException {
		String timeOfDay = null;
		String string1 = a;
		SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm:ss aa");
		SimpleDateFormat inputFormat = new SimpleDateFormat("mm/dd/yyyy hh:mm:ss aa");
		Date date = inputFormat.parse(string1);
		String string2 = outputFormat.format(date);
		Date actualdate = outputFormat.parse(string2);
		Calendar cal = Calendar.getInstance();
		int day = cal.get(Calendar.DAY_OF_MONTH);
		if (day == 1) {
			Date date1 = outputFormat.parse("11:59:59 PM");
			cal.setTime(date1);
			cal.add(Calendar.MONTH, -1);
			cal.add(Calendar.DAY_OF_MONTH, -1);
		} else {
			Date date2 = outputFormat.parse("11:59:59 PM");
			cal.setTime(date2);
			cal.add(Calendar.DAY_OF_MONTH, -1);
		}
		Date newDate = cal.getTime();
		// System.out.println(date + "----" + newDate);
		if (actualdate.after(newDate) && actualdate.before(outputFormat.parse("05:59:59 AM"))
				|| actualdate.equals(outputFormat.parse("05:59:59 AM"))) {
			timeOfDay = "LateNight";
		} else if (actualdate.after(outputFormat.parse("05:59:59 AM"))
				&& actualdate.before(outputFormat.parse("08:59:59 AM"))
				|| actualdate.equals(outputFormat.parse("08:59:59 AM"))) {
			timeOfDay = "Morning";
		} else if (actualdate.after(outputFormat.parse("04:59:59 PM"))
				&& actualdate.before(outputFormat.parse("07:59:59 PM"))
				|| actualdate.equals(outputFormat.parse("07:59:59 PM"))) {
			timeOfDay = "Evening";
		} else if (actualdate.after(outputFormat.parse("07:59:59 PM"))
				&& actualdate.before(outputFormat.parse("11:59:59 PM"))
				|| actualdate.equals(outputFormat.parse("11:59:59 PM"))) {
			timeOfDay = "Night";
		} else {
			timeOfDay = "dayTime";
		}
		return timeOfDay;
	}
    
    public void verbose(String[] args) {
        verbose = true;
        
        // check that we have the correct arguments
        if (args.length < 3) {
            System.out.println("Insufficient arguments. Exiting...");
            System.exit(0);
        }
        
        double minsup = Double.parseDouble(args[1]);
        
        if (minsup > 1 || minsup < 0) {
            System.out.println("Minimum support value out of bounds.");
            System.exit(0);
        }
        
        double minconf = Double.parseDouble(args[2]);

        if (minconf > 1 || minconf < 0) {
            System.out.println("Minimum confidence value out of bounds.");
            System.exit(0);
        }
        
        System.out.println("Minimum support: " + minsup);
        System.out.println("Minimum confidence: " + minconf);
        
        // one line of the csv file
        String line = "";
        Scanner lineScanner = new Scanner(System.in);
        
        HashMap<String, DashTreeNode> tough = new HashMap<>();
        
        // create a root node for the tree
        DashTreeNode root = new DashTreeNode(tough, new String[0], 0);
        tough.put("root", root);
        
        // text from each of the 46 comma-separated values
        String[] cols;

        // for timing
        Date start = new Date();

        // read each line of the file and divide comma-separated values
        System.out.println("\nScanning input file...");
        int row = 1;
        while (lineScanner.hasNext()) {
            // get the next line
            line = lineScanner.nextLine();

            cols = parseLine(line, 46);
            
            // select only the columns we're interested in
            String[] colArray = new String[] { cols[3], cols[5], cols[19] };

            // concatenate those columns into one string
            String indexString = cols[3] + "+" + cols[5] + "+" + cols[19];
            
            // map and tree stuff here
            // do we already have a node for this itemset?
            if (tough.containsKey(indexString)) {
                // then increase the count
                tough.get(indexString).increment(row);
            } else {
                // create one
                DashTreeNode z = new DashTreeNode(tough, colArray, row);
                tough.put(indexString, z);
            }

            row++;

        }
        
        System.out.println("Finished scanning. " + row + " lines read.");
        System.out.println(tough.size() + " candidate itemsets generated.");
        
        // tree successfully generated, prune low-support itemsets
        System.out.println("\nPruning itemsets with low support...");
        root.pruneRecursive(minsup, minconf, row, tough);
        
        System.out.println("Finished pruning. " + tough.size()
                           + " itemsets remain.");
        
        // generate rules for the surviving itemsets
        // another map for rules and their confidence levels
        HashMap<String, Double[]> ruff = new HashMap<>();
        
        // start at the root
        root.generateRules(ruff, tough);
        
        System.out.println(ruff.size() + " rules discovered.");
        System.out.println("\nPruning rules with low confidence...");
        
        String rule = "";
        Iterator it = ruff.keySet().iterator();        
        while(it.hasNext()) {
            rule = (String) it.next();
            if (ruff.get(rule)[0] < minconf) {
                System.out.println(rule + ": Pruned (c = " +
                                   String.format("%.3f", ruff.get(rule)[0]) + ")");
                try {
                    it.remove();                
                } catch (UnsupportedOperationException e){
                    
                } catch (IllegalStateException e) {
                    
                }
            } else {
                System.out.println("   " + rule + ": Preserved (c = " +
                                   String.format("%.3f", ruff.get(rule)[0]) + ")");
            }
        }
        
        System.out.println("");
        System.out.println("Final set of rules meeting mininum support and "
                           + "minimum confidence thresholds:");
        if (ruff.size() == 0) {
            System.out.println("No nodes remain after pruning. You may " 
                               + "need to use a lower minconf value.");
        }
        it = ruff.keySet().iterator();        
        while(it.hasNext()) {
            rule = (String) it.next();
            System.out.println(rule + ": s = " 
                               + String.format("%.3f", ruff.get(rule)[1]/row)
                               + "; c = "
                               + String.format("%.3f", ruff.get(rule)[0]));
        }

        // how much memory are we using?
        debugPrint(tough.size() + " items in the hash map/tree.");
        
        // how long did it take?
        Date end = new Date();
        double dur = end.getTime() - start.getTime();
        debugPrint("Duration: " + dur/1000.0 + " seconds.");

    }
    
    public void master(String[] args) {
        
        // check that we have the correct arguments
        if (args.length < 5) {
            System.out.println("Insufficient arguments. Exiting...");
            System.exit(0);
        }
        
        double minsup = Double.parseDouble(args[1]);
        
        if (minsup > 1 || minsup < 0) {
            System.out.println("Minimum support value out of bounds.");
            System.exit(0);
        }
        
        double minconf = Double.parseDouble(args[2]);

        if (minconf > 1 || minconf < 0) {
            System.out.println("Minimum confidence value out of bounds.");
            System.exit(0);
        }
        
        // check for the two input files here
        File file1 = new File(args[3]);
        if (file1.exists() == false) {
            System.out.println("File not found: " + args[3]);
            System.exit(0);            
        }

        File file2 = new File(args[4]);
        if (file2.exists() == false) {
            System.out.println("File not found: " + args[3]);
            System.exit(0);            
        }
        
        String line1 = "";
        String line2 = "";
        Scanner lineScanner1 = null;
        Scanner lineScanner2 = null;
        try {
            lineScanner1 = new Scanner(file1);
            lineScanner2 = new Scanner(file2);
        } catch (FileNotFoundException e) {
            // exception will never appear - already tested that files exist
        }
        
        HashMap<String, DashTreeNode> tough = new HashMap<>();
        
        // create a root node for the tree
        DashTreeNode root = new DashTreeNode(tough, new String[0], 0);
        tough.put("root", root);
        
        // text from each of the 46 comma-separated values
        String[] cols1 = null;
        String[] cols2 = null;

        // for timing
        Date start = new Date();

        // read each line of the file and divide comma-separated values
        int row = 1;
        while (lineScanner1.hasNext()) {
            // get the next line
            line1 = lineScanner1.nextLine();
            line2 = lineScanner2.nextLine();

            cols1 = parseLine(line1, 46);
            cols2 = parseLine(line2, 2);
            
            // select only the columns we're interested in
            String[] colArray = new String[] { 
                cols1[3],
                cols1[5],
                cols1[19],
                cols2[1] };

            // concatenate those columns into one string
            String indexString = cols1[3] + "+" + cols1[5]
                + "+" + cols1[19] + "+" + cols2[1];
            
            // map and tree stuff here
                // do we already have a node for this itemset?
                if (tough.containsKey(indexString)) {
                    // then increase the count
                    tough.get(indexString).increment(row);
                } else {
                    // create one
                    DashTreeNode z = new DashTreeNode(tough, colArray, row);
                    tough.put(indexString, z);
                }
//            }

            row++;

        }
                
        debugPrint("   FINISHED GENERATING ITEMSET TREE   \n");
        
        // tree successfully generated, prune low-support itemsets
        debugPrint("   PRUNING ITEMSETS   ");
        root.pruneRecursive(minsup, minconf, row, tough);

        // generate rules for the surviving itemsets
        // another map for rules and their confidence levels
        HashMap<String, Double[]> ruff = new HashMap<>();
        
        // start at the root
        root.generateRules(ruff, tough);
        
        String rule = "";
        Iterator it = ruff.keySet().iterator();        
        while(it.hasNext()) {
            rule = (String) it.next();
            if (ruff.get(rule)[0] < minconf) {
                try {
                    it.remove();                
                } catch (UnsupportedOperationException e){
                    
                } catch (IllegalStateException e) {
                    
                }
            }
        }
        
        System.out.println("Final set of rules meeting mininum support and "
                           + "minimum confidence thresholds:");
        it = ruff.keySet().iterator();        
        while(it.hasNext()) {
            rule = (String) it.next();
            System.out.println(rule + ": s = " 
                               + String.format("%.3f", ruff.get(rule)[1]/row)
                               + "; c = " 
                               + String.format("%.3f", ruff.get(rule)[0]));
        }
                        
        // how much memory are we using?
        debugPrint(tough.size() + " items in the hash map/tree.");
        
        // how long did it take?
        Date end = new Date();
        double dur = end.getTime() - start.getTime();
        debugPrint("Duration: " + dur/1000.0 + " seconds.");
    }
    
    // returns a string array of length 46 containing the text from
    // each column in one row
    public String[] parseLine(String line, int length) {
        
        // splice out text from each column
        Scanner colScanner = new Scanner(line).useDelimiter(",");
        
        String[] cols = new String[length];
        
        for (int i = 0; i < length; i++) {
            try {
                // treat the last column differently because there's no
                // trailing comma
                if (i == length - 1) {
                    if (colScanner.findWithinHorizon("\"", 2) != null) {
                        colScanner.useDelimiter("\"");
                        cols[i] = colScanner.next();
                    } else {
                        cols[i] = colScanner.next();
                    }
                // if a column begins with a " character, that means it
                // contains internal commas and needs special handling.
                // Capture everything inside the "..." and then reset the
                // delimieter to a comma.
                } else if (colScanner.findWithinHorizon("\"", 2) != null) {
                    colScanner.useDelimiter("\",");
                    cols[i] = colScanner.next();
                    colScanner.findWithinHorizon("\"", 1);
                    colScanner.useDelimiter(",");
                // just a plain column
                } else {
                    cols[i] = colScanner.next();
                }
            }
            catch (NoSuchElementException e) {
                cols[i] = "Error here.";
            }
        }
        
        return cols;
    }
    
    // testing for the hashmap and tree generation procedure
    // only processes the first 5 rows
    public void testMapAndTree() {
        
        // one line of the csv file
        String line = "";
        Scanner lineScanner = new Scanner(System.in);
        
        HashMap<String, DashTreeNode> tough = new HashMap<>();
        
        // create a root node for the tree
        DashTreeNode root = new DashTreeNode(tough, new String[0], 0);
        tough.put("root", root);
        
        // text from each of the 46 comma-separated values
        String[] cols;

        // for timing
        Date start = new Date();

        // read each line of the file and divide comma-separated values
        int row = 1;
        while (lineScanner.hasNext()) {
            // get the next line
            line = lineScanner.nextLine();

            cols = parseLine(line, 46);
            
            // select only the columns we're interested in
            String[] colArray = new String[] { cols[3], cols[5], cols[19] };

            // concatenate those columns into one string
            String indexString = cols[3] + "+" + cols[5] + "+" + cols[19];
            
            // map and tree stuff here
//            if (row <= 20) {
                // do we already have a node for this itemset?
                if (tough.containsKey(indexString)) {
                    // then increase the count
                    tough.get(indexString).increment(row);
                } else {
                    // create one
                    DashTreeNode z = new DashTreeNode(tough, colArray, row);
                    tough.put(indexString, z);
                }
//            }

            row++;

        }
        
        debugPrint("   FINISHED GENERATING TREE   \n");
        debugPrint("   TREE TRAVERSAL   ");
        
        // tree successfully(?) generated, now output a traversal
        String indent = "";
        root.printRecursive(indent);
        
        // how much memory are we using?
        debugPrint(tough.size() + " items in the hash map.");
        debugPrint("(Equal number of nodes in the tree.)");
        
        // how long did it take?
        Date end = new Date();
        double dur = end.getTime() - start.getTime();
        debugPrint("Duration: " + dur/1000.0 + " seconds.");
        
    }
    
    // test the column splicing procedure
    // separates and prints out columns from one out of every 1000 rows
    // (roughly 163 rows)
    public void testPrint() {
        
        // one line of the csv file
        String line = "";
        Scanner lineScanner = new Scanner(System.in);

        // text from each of the 46 comma-separated values
        String[] cols;

        // for timing
        Date start = new Date();

        // read each line of the file and divide comma-separated values
        int row = 1;
        while (lineScanner.hasNext()) {
            // get the next line
            line = lineScanner.nextLine();

            cols = parseLine(line, 2);
            
            // for testing purposes, only printing out a few lines
//            if (row % 1000 == 0) {
                debugPrint(Integer.toString(row));
                for (int i = 0; i < cols.length; i++) {
                    debugPrint(i + ": " + cols[i]);
                }
//            }
            
            row++;

        }
        
        // how long did it take?
        Date end = new Date();
        double dur = end.getTime() - start.getTime();
        debugPrint("Duration: " + dur/1000.0 + " seconds.");
    }

    // separate class for the itemsets in the tree/lattice 
    // slide 8 of Tan, Steinbach, Kumar
    private class DashTreeNode {
        String[] items;
        int count;
        
        // ensure that each node is only updated once per row
        int lastUpdateRow;
        
        // subsets are nodes with one fewer item in the set
        // (traveling "up" the lattice)
        // at the top is the root node which represents the null set
        DashTreeNode[] subsetNodes;
        
        // supersets are nodes which contain this itemset
        // (traveling "down" the lattice)
        DashTreeNode[] supersetNodes;
        
        public DashTreeNode(HashMap<String, DashTreeNode> tough,
                            String[] items,
                            int row) {
            // create this node
            this.items = items.clone();
            count = 1;
            lastUpdateRow = row;

            // begin with zero superset nodes
            supersetNodes = new DashTreeNode[0];
            
            debugPrint("NEW NODE: " + toString());

            // create and link subset nodes
            if (items.length == 0) {
                // this is the root node, no subsets possible
                subsetNodes = null;
            } else {
                subsetNodes = new DashTreeNode[items.length];
                addSubsetNodes(items, tough);
            }
            
        }
        
        public void addSubsetNodes(String[] items,
                                   HashMap<String, DashTreeNode> tough) {
            
            DashTreeNode subsetNode;

            // create each subset node
            if (items.length == 1) {
                // the only subset node will be the root node
                subsetNode = tough.get("root");
                subsetNodes[0] = subsetNode;
                subsetNode.addSupersetNode(this);
            } else {
                // several subsets
                for (int i = 0; i < items.length; i++) {

                    // items that will be in the subset
                    String[] subsetArray = new String[items.length - 1];

                    // concatenated string of above items
                    String subsetString = "";
                    String sep = "";

                    // build a subset array by removing one item from the
                    // current itemset and copying the others
                    for (int j = 0; j < items.length; j++) {
                        if (j < i) {
                            subsetArray[j] = items[j];
                            subsetString = subsetString + sep + subsetArray[j];
                            sep = "+";
                        } else if (j > i) {
                            subsetArray[j-1] = items[j];
                            subsetString = subsetString + sep
                                + subsetArray[j-1];
                            sep = "+";
                        }
                    }

                    // check if such a subset node already exists
                    if (tough.containsKey(subsetString) == false) {
                        // node does not exist so create it
                        subsetNode = new DashTreeNode(tough, subsetArray,
                                                     lastUpdateRow);
                        tough.put(subsetString, subsetNode);
                    } else {
                        // node does exist so get a handle to it and increment
                        subsetNode = tough.get(subsetString);                   
                        subsetNode.increment(lastUpdateRow);
                    }

                    // add the subset node to my list of subset nodes
                    subsetNodes[i] = subsetNode;

                    // add this node to that subset node's superset list
                    subsetNode.addSupersetNode(this);
                }
            }
        }
        
        public void addSupersetNode(DashTreeNode newSupersetNode) {
            
            // create a new array one item larger than the current one
            DashTreeNode[] supersetNodes2 = 
                new DashTreeNode[supersetNodes.length+1];
            
            // copy in all existing values
            for (int i = 0; i < supersetNodes.length; i++) {
                supersetNodes2[i] = supersetNodes[i];
            }
            
            // add new superset node
            supersetNodes2[supersetNodes.length] = newSupersetNode;            
            
            // replace previous array with new one
            supersetNodes = supersetNodes2;
        }
        
        public String itemsToString() {
            if (items.length < 1) {
                return "root";
            } else {
                String output = items[0];
                for (int i = 1; i < items.length; i++) {
                    output = output + "+" + items[i];
                }
                return output;
            }
        }
        
        public String toString() {
            String output = "Values:";
            for (int i = 0; i < items.length; i++) {
                output = output + " " + items[i];
            }
            
            output = output + "; count: " + count
                + "; last updated at row: " + lastUpdateRow;
            
            return output;
        }
        
        public void increment(int row) {
            if (row > lastUpdateRow) {
                count++;
                lastUpdateRow = row;          
                debugPrint("UPDATED NODE: " + toString());
                
                if (subsetNodes.length > 1) {
                    for (int i = 0; i < subsetNodes.length; i++) {
                        subsetNodes[i].increment(row);
                    }
                }
            }
        }
        
        // returns true if an itemset should be pruned
        public boolean pruneRecursive(double minsup, double minconf, int row,
                                     HashMap<String, DashTreeNode> tough) {

            double support = (double) count / (double) row;
            
            debugPrint("Node: " + itemsToString() 
                               + "; support: " + support);
            
            // prune low support except for root node
            if (support < minsup && items.length != 0) {
                // if my support is low, delete supersets and return true
                debugPrint("Low support. Deleting supersets " 
                                   + "and this set.");
                deleteRecursive(tough);
                return true;
            } else {
                debugPrint("Sufficient support. Checking supersets.");
            }
            
            // otherwise, check supersets and then return false            
            for (int i = 0; i < supersetNodes.length; i++) {
                
                // skip any previously-pruned supersets
                if (supersetNodes[i] == null) {
                    continue;
                }
                
                // if the support of the superset is low, destroy it
                // (super-supersets will have already been destroyed)
                if (supersetNodes[i].pruneRecursive(minsup, minconf, row,
                                                   tough)) {
                    supersetNodes[i].destroy(tough);
                } else {
                    debugPrint("Sufficient support, not pruning "
                                       + itemsToString());                    
                }
            }
            
            return false;

        }
        
        // deletes all supersets without deleting the itemset itself
        public void deleteRecursive(HashMap<String, DashTreeNode> tough) {
            for (int i = 0; i < supersetNodes.length; i++) {
                if (supersetNodes[i] != null) {
                    supersetNodes[i].destroy(tough);
                }
            }
        }

        public void printRecursive(String indent) {
            debugPrint(indent + "THIS NODE: " + toString());
            debugPrint(indent + "Supersets: " + supersetNodes.length);
            for (int i = 0; i < supersetNodes.length; i++) {
                supersetNodes[i].printRecursive(indent + "  ");
            }
        }

        // remove the reference from every subset and then recurse
        // (basically destroying all supersets AND destroying the itemset)
        public void destroy(HashMap<String, DashTreeNode> tough) {
            debugPrint("Destroying " + itemsToString());
            tough.remove(itemsToString());
            debugPrint(tough.size() + " items remaining in the tree.");
            for (int i = 0; i < subsetNodes.length; i++) {
                for (int j = 0; j < subsetNodes[i].supersetNodes.length; j++) {
                    if (subsetNodes[i].supersetNodes[j] != null &&
                        subsetNodes[i].supersetNodes[j].itemsToString()
                        .equals(itemsToString())) {
                        debugPrint("Removing reference from " 
                                           + subsetNodes[i].itemsToString());
                        subsetNodes[i].supersetNodes[j] = null;
                    }
                }
            }
            
            for (int i = 0; i < supersetNodes.length; i++) {
                if (supersetNodes[i] != null) {
                    supersetNodes[i].destroy(tough);
                }
            }
        }
        
        public void generateRules(HashMap<String, Double[]> ruff,
                                HashMap<String, DashTreeNode> tough) {
            
            // traverse down until I reach a 3-itemset
            if (items.length < 3) {
                debugPrint("Small item set.");
                for (int i = 0; i < supersetNodes.length; i++) {
                    if (supersetNodes[i] != null) {
                        supersetNodes[i].generateRules(ruff, tough);
                    }
                }
            } else if (items.length == 3) {
                debugPrint("3-itemset.");
                // generate 12 (6 + 6) rules and store in hashmap
                
                // 2-rules
                // A B      B A
                // A C      C A
                // B C      C B
                checkRule(items[0], items[1], items[0] + "+" + items[1],
                          ruff, tough);
                checkRule(items[1], items[0], items[0] + "+" + items[1],
                          ruff, tough);
                checkRule(items[0], items[2], items[0] + "+" + items[2],
                          ruff, tough);
                checkRule(items[2], items[0], items[0] + "+" + items[2],
                          ruff, tough);
                checkRule(items[1], items[2], items[1] + "+" + items[2],
                          ruff, tough);
                checkRule(items[2], items[1], items[1] + "+" + items[2],
                          ruff, tough);
                
                // 3-rules
                // A BC     BC A
                // B AC     AC B
                // C AB     AB C
                checkRule(items[0], items[1] + "+" + items[2], itemsToString(),
                          ruff, tough);
                checkRule(items[1] + "+" + items[2], items[0], itemsToString(),
                          ruff, tough);
                checkRule(items[1], items[0] + "+" + items[2], itemsToString(),
                          ruff, tough);
                checkRule(items[0] + "+" + items[2], items[1], itemsToString(),
                          ruff, tough);
                checkRule(items[2], items[0] + "+" + items[1], itemsToString(),
                          ruff, tough);
                checkRule(items[0] + "+" + items[1], items[2], itemsToString(),
                          ruff, tough);

                // traverse down to each 4-itemset
                for (int i = 0; i < supersetNodes.length; i++) {
                    if (supersetNodes[i] != null) {
                        supersetNodes[i].generateRules(ruff, tough);
                    }
                }
            } else {
                debugPrint("4-itemset");
            
                // generate 14 more rules and store in hashmap
                // A BCD    B ACD    C ABD    D ABC
                // BCD A    ACD B    ABD C    ABC D
                checkRule(items[0], items[1] + "+" + items[2] + "+" + items[3],
                        itemsToString(), ruff, tough);
                checkRule(items[1] + "+" + items[2] + "+" + items[3], items[0],
                        itemsToString(), ruff, tough);
                checkRule(items[1], items[0] + "+" + items[2] + "+" + items[3],
                        itemsToString(), ruff, tough);
                checkRule(items[0] + "+" + items[2] + "+" + items[3], items[1],
                        itemsToString(), ruff, tough);
                checkRule(items[2], items[0] + "+" + items[1] + "+" + items[3],
                        itemsToString(), ruff, tough);
                checkRule(items[0] + "+" + items[1] + "+" + items[3], items[2],
                        itemsToString(), ruff, tough);
                checkRule(items[3], items[0] + "+" + items[1] + "+" + items[2],
                        itemsToString(), ruff, tough);
                checkRule(items[0] + "+" + items[1] + "+" + items[2], items[3],
                        itemsToString(), ruff, tough);
                // AB CD    CD AB
                // AC BD    BD AC
                // BC AD    AD BC
                checkRule(items[0] + "+" + items[1], items[2] + "+" + items[3],
                        itemsToString(), ruff, tough);
                checkRule(items[2] + "+" + items[3], items[0] + "+" + items[1],
                        itemsToString(), ruff, tough);
                checkRule(items[0] + "+" + items[2], items[1] + "+" + items[3],
                        itemsToString(), ruff, tough);
                checkRule(items[1] + "+" + items[3], items[0] + "+" + items[2],
                        itemsToString(), ruff, tough);
                checkRule(items[1] + "+" + items[2], items[0] + "+" + items[3],
                        itemsToString(), ruff, tough);
                checkRule(items[0] + "+" + items[3], items[1] + "+" + items[2],
                        itemsToString(), ruff, tough);
            }
        }
        
        
        public void checkRule(String lhsString,
                              String rhsString,
                              String union,
                              HashMap<String, Double[]> ruff,
                              HashMap<String, DashTreeNode> tough) {
            
            // check if rule already exists
            if (ruff.containsKey(lhsString + "=>" + rhsString) == false) {

                String rule = lhsString + "=>" + rhsString;
                double s1 = (double) tough.get(union).count;
                double s2 = (double) tough.get(lhsString).count;
                double s3 = s1/s2;
                debugPrint(rule + ": " + s3);
                
                ruff.put(rule, new Double[] {s3, s1} );
            }
        }
    }    
}