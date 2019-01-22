package com.ibm.ode.pkg.parserGenerator.service;
import com.ibm.ode.pkg.parserGenerator.GeneratorException;
import java.util.Hashtable;

public class Cmvc2B390 
{
  public static final int fileType=0;  //part type
  public static final int sourceFile=1;//internal part name
  public static final int targetFile=2;//external part name
  public static final int jclinLkedParms=3;//Module link-edit parameters  
  public static final int distLib=4;//distribution library
  private static final String[] cmvcLabels={"FILETYPE","SOURCEFILE",
          "TARGETFILE","JCLINLKEDPARMS","DISTLIB"};
  private static Hashtable labels = getLabels();
  private static Hashtable required = getRequired();
  private static Hashtable getLabels()
  { 
    Hashtable labels=new Hashtable();
    labels.put(cmvcLabels[fileType],"CLASS");
    labels.put(cmvcLabels[sourceFile],"MOD");
    labels.put(cmvcLabels[targetFile],"DISTNAME");
    labels.put(cmvcLabels[jclinLkedParms],"LECHAR");
    labels.put(cmvcLabels[distLib],"DISTLIB");
    return labels;
  }
  private static Hashtable getRequired()
  { 
    Hashtable required=new Hashtable();
    required.put(cmvcLabels[fileType],new Boolean(true));
    required.put(cmvcLabels[sourceFile],new Boolean(true));
    required.put(cmvcLabels[targetFile],new Boolean(true));
    required.put(cmvcLabels[jclinLkedParms],new Boolean(true));
    required.put(cmvcLabels[distLib],new Boolean(true));
    return required;
  }
  public static boolean isRequired(int cmvcLabel)
  {
    boolean result = ((Boolean)required.get(cmvcLabels[cmvcLabel])).booleanValue();
    return result;
  }
  public static String getMVSLabel(int cmvcLabel) 
    throws GeneratorException
  {
    if (cmvcLabel<0||cmvcLabel>cmvcLabels.length)
      throw new GeneratorException("Invalid CMVC label index "+cmvcLabel);
    String result = (String)(labels.get(cmvcLabels[cmvcLabel]));
    if (result==null) throw new GeneratorException("MVS label not found for CMVC label "+cmvcLabel);
    return result;
  }
  public static boolean isRequired(String cmvcLabel)
  {
    cmvcLabel=cmvcLabel.trim().toUpperCase();
    boolean result = ((Boolean)required.get(cmvcLabel)).booleanValue();
    return result;
  }
  public static String getMVSLabel(String cmvcLabel) 
    throws GeneratorException
  {
    cmvcLabel=cmvcLabel.trim().toUpperCase();
    String result = (String)labels.get(cmvcLabel);
    if (result==null) throw new GeneratorException("MVS label not found for CMVC label "+cmvcLabel);
    return result;
  }
  public static void main (String[] args) 
    throws GeneratorException
  {
    Cmvc2B390 cmvcMap = new Cmvc2B390();
    System.out.println(cmvcMap.isRequired(Cmvc2B390.fileType));
    System.out.println(cmvcMap.getMVSLabel(Cmvc2B390.fileType));
  }
}