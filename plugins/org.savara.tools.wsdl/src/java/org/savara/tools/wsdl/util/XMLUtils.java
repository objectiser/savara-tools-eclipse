/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.savara.tools.wsdl.util;

import java.io.ByteArrayOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.scribble.protocol.model.ModelObject;
import org.scribble.protocol.model.ModelProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This class defines a set of XML related utility functions.
 */
public final class XMLUtils {
	
	private static final String NS_LABEL = "ns";

	/**
	 * This class converts a DOM representation node to text.
	 * @param node
	 * @return
	 * @throws Exception
	 */
	public static final String toText(Node node) throws Exception {
		String ret = null;
		try {
			// Transform the DOM represent to text
			ByteArrayOutputStream xmlstr= new ByteArrayOutputStream();
			DOMSource source=new DOMSource(node);
			//source.setNode(node);
			
			StreamResult result=new StreamResult(xmlstr);			
			Transformer trans= TransformerFactory.newInstance().newTransformer();
			trans.transform(source, result);
			
			xmlstr.close();
			
			ret = new String(xmlstr.toByteArray());
			
			if ((node instanceof Document) == false) {				
				// Strip off any <?xml> header
				int index=ret.indexOf("<?xml");
				if (index != -1) {
					index = ret.indexOf("<", 1);
					if (index != -1) {
						ret = ret.substring(index);
					} else {
						index = ret.indexOf("?>");
						if (index != -1) {
							index += 2;
							
							// Remove any trailing whitespaces after XML header
							while (index < ret.length() &&
									Character.isWhitespace(ret.charAt(index))) {
								index++;
							}
							
							ret = ret.substring(index);
						}
					}
				}
			}

		} catch(Exception e) {
			throw new Exception("Failed to transform DOM representation into text", e);
		}
		if (ret != null) {
			 return format(ret);
		}
		return ret;
	}
	
	
	/**
	 * Format the xml to well print.
	 * 
	 * @param xmltext
	 * @return
	 */
	public static final String format(String xmltext) {
		String ret=xmltext;
		int pos=0;
		int prevpos=0;
		StringBuffer buf=new StringBuffer();
		int level=0;
		
		while ((pos=ret.indexOf('<', prevpos)) != -1) {
			
			if (prevpos < pos &&
					ret.substring(prevpos, pos).trim().length() > 0 &&
					ret.charAt(prevpos-1) != '?') {
				
				if (ret.charAt(prevpos) == '\r' &&
						ret.charAt(prevpos+1) == '\n') {
					prevpos += 2;
				}
				for (int i=0; i < level; i++) {
					buf.append("    ");
				}
				
				buf.append(ret.substring(prevpos, pos).trim());
				buf.append("\r\n");
			}
			
			int endpos=ret.indexOf('>', pos);
		
			if (endpos > 0) {
				boolean noreturn=false;
				
				if (pos > 0 && ret.charAt(pos+1) == '/') {
					level--;
				}
				
				for (int i=0; i < level; i++) {
					buf.append("    ");
				}
				buf.append(ret.substring(pos, endpos+1));
				
				if (ret.charAt(endpos-1)== '?') {
					//noreturn = true;
					
				} else if (ret.charAt(endpos-1) == '/') {
					// Ignore
				} else if (pos > 0 && ret.charAt(pos+1) == '/') {
					// Ignore
					
				} else if (pos > 0 && ret.charAt(pos+1) == '!') {
					// Ignore
					
				} else {
					level++;
				}
							
				if (noreturn == false) {
					buf.append("\r\n");
				}
				
				pos = endpos+1;
			}
			
			prevpos = pos;
		}
		
		if (prevpos != -1 &&
				ret.substring(prevpos).trim().length() > 0) {
			buf.append(ret.substring(prevpos));
		}
		
		ret = buf.toString();
		
		return(ret);
	}
	
	public static Node getNode(String text) throws Exception {
		Node ret=null;
		
		// Transform the text representation to DOM
		DocumentBuilderFactory fact=DocumentBuilderFactory.newInstance();
		fact.setNamespaceAware(true);
		
		java.io.InputStream xmlstr=
			new java.io.ByteArrayInputStream(text.getBytes());

		DocumentBuilder builder=fact.newDocumentBuilder();
		org.w3c.dom.Document doc=builder.parse(xmlstr);
		
		ret = doc.getDocumentElement();

		return(ret);
	}

	/**
	 * This method returns the localname part of the supplied
	 * qualified name.
	 * 
	 * @param qname The qualified name
	 * @return The localname part
	 */
	public static String getLocalname(String qname) {
		String ret=qname;
		int pos=0;
		
		if (qname != null && ((pos=qname.indexOf(':')) != -1)) {
			ret = qname.substring(pos+1);
		}
		
		return(ret);
	}
	
	/**
	 * This method returns the prefix associated with the supplied namespace.
	 * 
	 * @param namespace The namespace
	 * @param nsMap The existing namespace prefix mappings
	 * @return The prefix
	 */
	public static String getPrefix(String namespace, java.util.Map<String,String> nsMap) {
		String prefix=null;
		
		prefix = nsMap.get(namespace);
		
		if (prefix == null) {
			prefix = NS_LABEL+(nsMap.size()+1);
			nsMap.put(namespace, prefix);
		}

		return(prefix);
	}
	
	/**
	 * This method calculates the start and end position of a supplied DOM element, within
	 * the supplied text contents, and sets the values on the supplied ModelObject.
	 * 
	 * @param obj The ModelObject to be initialized
	 * @param contents The text contents
	 * @param elem The DOM element to be located in the text
	 */
	public static void setStartAndEndPosition(ModelObject obj, String contents, org.w3c.dom.Element elem) {

		if (contents != null) {
			org.w3c.dom.NodeList nl=elem.getOwnerDocument().getElementsByTagName(elem.getNodeName());
			int elempos=-1;
			
			for (int i=0; elempos == -1 && i < nl.getLength(); i++) {
				if (nl.item(i) == elem) {
					elempos = i;
				}
			}
			
			if (elempos != -1) {
				int startpos=-1;
					
				for (int i=0; i <= elempos; i++) {
					int val1=contents.indexOf("<"+elem.getNodeName()+">", startpos+1);
					int val2=contents.indexOf("<"+elem.getNodeName()+" ", startpos+1);
					
					if (val1 == -1 && val2 != -1) {
						startpos = val2;
					} else if (val1 != -1 && val2 == -1) {
						startpos = val1;
					} else if (val1 == -1 && val2 == -1) {
						// TODO: Error condition
						break;
					} else if (val1 > val2) {
						startpos = val2;
					} else {
						startpos = val1;
					}
				}
				
				if (startpos != -1) {
					obj.getProperties().put(ModelProperties.START_LOCATION, startpos);
					
					// Check if single node
					int p1=contents.indexOf('>', startpos);
					
					if (p1 != -1 && contents.charAt(p1-1) == '/') {
						obj.getProperties().put(ModelProperties.END_LOCATION, p1);
					} else {
					
						org.w3c.dom.NodeList enl=elem.getElementsByTagName(elem.getNodeName());
						
						int endpos=startpos;
						String nodetxt="</"+elem.getNodeName()+">";
						
						for (int i=0; endpos != -1 && i <= enl.getLength(); i++) {
							endpos = contents.indexOf(nodetxt, endpos+1);
						}
						
						if (endpos != -1) {
							obj.getProperties().put(ModelProperties.END_LOCATION,
											endpos+nodetxt.length()-1);
						}
					}
				}
			}
		}
	}
}
