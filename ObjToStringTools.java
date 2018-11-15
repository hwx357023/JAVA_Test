package com.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;


public class ObjToStringTools {
    //test
    /*public void alarm(List<LicAlarmPrdChange> chgs) {
        SimpleDateFormat myFmt = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
        Date d = new Date();
        StringBuffer sb = new StringBuffer();
        sb.append("----------------ALARM " + myFmt.format(d)
                + "----------------\n");
        objToXml(sb, chgs, 0);
        sb.append("\n----------------ALARM END---------------------------------\n");
        System.out.println(sb.toString());
        // (new TestIOWrite()).write(sb.toString().getBytes());
    }*/
	
   /*public static void lkParserOutput(LiclkParserRslt lkParserRslt){
	   
	   SimpleDateFormat myFmt = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
	   Date d = new Date();
       StringBuffer sb = new StringBuffer();
       sb.append("----------------PARSER " + myFmt.format(d)
               + "----------------\n");
       objToXml(sb, lkParserRslt, 0);
       sb.append("\n----------------PARSER END----------------\n");
       System.out.println(sb.toString());

    }*/
	public static void show(String title, Object obj) {
		StringBuffer sb = new StringBuffer();
	       sb.append("----------------"+title+"----------------\n");
	       objToXml(sb, obj, 0);
	       sb.append("\n--------------"+title+" END------------\n");
	       LogImpl.print(sb.toString());
	}
   
    /*
     * obj to xml
     * @param nodeClass 缩进制表符的个数
     */
    public static void objToXml(StringBuffer sb, Object obj, int nodeClass) {

        if (obj == null) {
            sb.append("null");
            return;
        }

        Method[] mds = obj.getClass().getMethods();
        String tabStr = getTabStr(nodeClass);
        ObjectType objType = getType(obj);

        if (objType == ObjectType.LIST) {
            List ls = (List) obj;
            for (Object o : ls) {
                sb.append(tabStr+"<L>\n");
                objToXml(sb, o, nodeClass+1);
                sb.append(tabStr+"</L>\n");
            }
            return;
        }

        if (objType == ObjectType.PURE) {
            sb.append(obj);
            return;
        }

        if (objType == ObjectType.ARRAY) {
            return;
        }

        boolean isPure = true;

        for (Method m : mds) {
            String name = m.getName();
            if ((name.startsWith("get") || name.startsWith("is"))
                    && !name.equals("getClass")) {
                isPure = false;
                try {
                    Object mRet = m.invoke(obj);
                    String s = methodToName(name);
                    ObjectType objTypeInner = getType(mRet);
                    if (mRet == null) {
                        sb.append(tabStr + "<" + s + ">");
                        sb.append("null");
                        sb.append("</" + s + ">\n");
                    } else if (objTypeInner == ObjectType.PURE) {
                        sb.append(tabStr + "<" + s + ">");
                        sb.append(mRet);
                        sb.append("</" + s + ">\n");
                    } else if (objTypeInner == ObjectType.LIST) {
                        List ls = (List) mRet;
                        for (Object o : ls) {
                        	if(getType(o)==ObjectType.PURE) {
                        		sb.append(tabStr + "<" + s + ">"+o+"</"+s+">\n");
                        	}else {
	                            sb.append(tabStr + "<" + s + ">\n");
	                            objToXml(sb, o, nodeClass + 1);
	                            sb.append(tabStr + "</" + s + ">\n");
                        	}
                        }
                    } else if (objTypeInner == ObjectType.GENERAL) {
                        sb.append(tabStr + "<" + s + ">\n");
                        objToXml(sb, mRet, nodeClass + 1);
                        sb.append(tabStr + "</" + s + ">\n");
                    } else
                        continue;

                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    System.out.println("name:"+name+",class:"+obj.getClass());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

        if (isPure) {
            sb.append(obj);
        }
    }

    // obj to json string
    @SuppressWarnings("rawtypes")
    public static void objToJson(StringBuffer sb, Object obj) {

        if (obj == null) {
            sb.append("null");
            return;
        }

        Method[] mds = obj.getClass().getMethods();

        if (obj instanceof List) {
            List ls = (List) obj;
            for (Object o : ls) {
                objToJson(sb, o);
            }
            return;
        }

        if (obj instanceof Integer || obj instanceof String
                || obj instanceof Long || obj instanceof Byte
                || obj instanceof Short || obj instanceof Enum
                || obj instanceof Boolean || obj instanceof Character
                || obj instanceof Double) {
            sb.append(obj);
            return;
        }

        if (obj.getClass().isArray()) {
            // sb.append("[unkown]");
            sb.append("[");
            Class cls = obj.getClass().getComponentType();
            boolean hasNode = false;
            if (cls == byte.class) {
            } else if (cls == short.class) {
            } else if (cls == char.class) {
            } else if (cls == int.class) {
            } else if (cls == long.class) {
            } else if (cls == float.class) {
            } else if (cls == double.class) {
            } else {
                Object[] objs = (Object[]) obj;
                for (Object e : objs) {
                    hasNode = true;
                    objToJson(sb, e);
                    sb.append(",");
                }
            }
            if (hasNode) {
                sb.delete(sb.length() - 1, sb.length());
            }
            sb.append("]");
            return;
        }

        boolean isPure = true;
        boolean isFirst = true;

        for (Method m : mds) {
            String name = m.getName();
            if ((name.startsWith("get") || name.startsWith("is"))
                    && !name.equals("getClass")) {
                isPure = false;
                if (isFirst) {
                    sb.append("{");
                    isFirst = false;
                }
                try {
                    Object mRet = m.invoke(obj);
                    sb.append(methodToName(name) + ":");
                    // 递归
                    objToJson(sb, mRet);

                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                sb.append(",");
            }
        }

        if (isPure) {
            sb.append(obj);
        } else {
            // 去掉最后一个,
            sb.delete(sb.length() - 1, sb.length());
            sb.append("}");
        }
    }

    protected static String methodToName(String m) {
        if (m.startsWith("is"))
            return m;
        char ch = m.charAt(3);
        char lowerCh = Character.toLowerCase(ch);
        String s = m.substring(4);
        return lowerCh + s;
    }

    protected static String getTabStr(int tabNum) {
        String s = "";
        for (int i = 0; i < tabNum; i++) {
            s += "    ";
        }
        return s;
    }

    private enum ObjectType {
        GENERAL, PURE, LIST, ARRAY
    }

    protected static ObjectType getType(Object obj) {
        if (obj == null)
            return null;
        if (obj instanceof List) {
            return ObjectType.LIST;
        }
        if (obj.getClass().isArray())
            return ObjectType.ARRAY;
        if (obj instanceof Integer || obj instanceof String
                || obj instanceof Long || obj instanceof Byte
                || obj instanceof Short || obj instanceof Enum
                || obj instanceof Boolean || obj instanceof Character
                || obj instanceof Double || obj instanceof Float) {
            return ObjectType.PURE;
        }
        return ObjectType.GENERAL;
    }
}
