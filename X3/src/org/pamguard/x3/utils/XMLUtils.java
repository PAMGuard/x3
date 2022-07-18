package org.pamguard.x3.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Useful functions for parsing XML strings. 
 * 
 * @author Jamie Macaulay
 * @author Doug Gillespie
 *
 */
public class XMLUtils {
	
	
	/**
	 * @param nodeList
	 * @return nodeList as Node[]
	 */
	public static Node[] getNodeArray(NodeList nodeList){
		Node[] nodes = new Node[nodeList.getLength()];
		for (int i=0;i<nodeList.getLength();i++){
			nodes[i]=nodeList.item(i);
		}
		return nodes;
	}

	/**
	 * @param nodeArray
	 * @return nodeArray as ArrayList<Node>
	 */
	public static ArrayList<Node> getNodeArrayList(Node[] nodeArray){
		return new ArrayList<Node>(Arrays.asList(nodeArray));
	}
	
	/**
	 * @param nodes - ArrayList of nodes to look through for one with specific name
	 * @param name - name to match
	 * @return a copy of "nodes" without ones that are not named name 
	 */
	public static ArrayList<Node> getNodesOfName(ArrayList<Node> nodes,String name){
		ArrayList<Node> nodesLeft = new ArrayList<Node>();
		for (int i=0;i<nodes.size();i++){
			if (nodes.get(i).getNodeName()==name){
				nodesLeft.add(nodes.get(i));
			}
		}
		return nodesLeft;
	}
	
	/**
	 * @param nodes - ArrayList of nodes to look through for attributes
	 * @param attributes - a HashMap of <attribute, attributeValues> attributeVAlue can be null if it is not required to be equal to anything
	 * @return a copy of "nodes" without ones that don't match 
	 */
	public static ArrayList<Node> getNodesWithAttributes(ArrayList<Node> nodes,HashMap<String,String> attributes){
		ArrayList<Node> nodesLeft = new ArrayList<Node>();
		for (int i=0;i<nodes.size();i++){
			
			int attMat=0;
			for (String key : attributes.keySet()){
				Node match = nodes.get(i).getAttributes().getNamedItem(key); // works simply as cant has 2 attributes of same name?
				if( match != null){
					if (attributes.get(key) == null || attributes.get(key) == match.getNodeValue()){ //should maybe be getTextContent()
						attMat++;
					}
				}
			}
			
			if (attMat==attributes.size()){
				nodesLeft.add(nodes.get(i));
			}
		}
		return nodesLeft;
	}
	
	/**
	 * @param nodes - ArrayList of nodes to look through for child nodes
	 * @param children - a HashMap of <childNames, childValues> childVAlue can be null if it is not required to be equal to anything
	 * @return a copy of "nodes" without ones that don't match 
	 */
	public static ArrayList<Node> getNodesWithChildren(ArrayList<Node> nodes, HashMap<String,String> children){
		ArrayList<Node> nodesLeft = new ArrayList<Node>();
		for (int i=0;i<nodes.size();i++){
			int cldMat=0;
			for (String key : children.keySet()){
				ArrayList<Node> nameMatches = getNodesOfName(getNodeArrayList(getNodeArray(nodes.get(i).getChildNodes())), key);
				for (Node match:nameMatches){
					if( children.get(key) == null || children.get(key) == match.getTextContent() ){
						//value isn't required or matches
						cldMat++;
					}
//					if (attributes.get(key) == null || attributes.get(key) == match.getNodeValue()){
//						attMat++;
//					}
				}
			}
			if (cldMat==children.size()){
				nodesLeft.add(nodes.get(i));
			}
		}
		return nodesLeft;
	}

	/**
	 * Get node content from a NodeList by name.
	 * @param names - a list of the names to search for
	 * @param nodeList - a list of the the contents for each name, 
	 * @return a list of corresponding node vales - null if the value was not found. 
	 */
	public static HashMap<String, String>  getInnerNodeContent(String[]  names, NodeList nodeList) {
		//String[] output =  new String[names.length];
		
		HashMap<String, String> output = new HashMap<String, String>(names.length);

		if(nodeList!=null && nodeList.getLength() > 0) {
			for (int i=0; i<nodeList.getLength(); i++) {

				//System.out.println("Child Nodes len: " +  nodeList.item(i).getChildNodes().getLength()); 

				for (int j=0; j< nodeList.item(i).getChildNodes().getLength(); j++) {

					//System.out.println("Child Node Names: "+ nodeList.item(i).getChildNodes().item(j).getNodeName()+ " attributes" + nodeList.item(i).getChildNodes().item(j).getAttributes() + "   "  +j);

					String content = nodeList.item(i).getChildNodes().item(j).getTextContent().trim();

					for (int k=0; k<names.length; k++) {
						if (nodeList.item(i).getChildNodes().item(j).getNodeName().equals(names[k])) {
							output.put(names[k], content.trim());
						}
					}
				}
			}
		}
		
		return output;
	}

}
