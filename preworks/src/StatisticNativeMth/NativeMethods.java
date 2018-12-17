package StatisticNativeMth;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.*;

import soot.*;
import soot.options.Options;
import subClassTemplate.AppendContentToFile;

public class NativeMethods {
	//private static final Logger logger = LoggerFactory.getLogger(convertNativeMethods.class);
	//public static final String apiDir = "modeling/api";
	//public static String DSHome = System.getenv("DROIDSAFE_SRC_HOME");
	private String applicatoinClassPath;/**要加载应用类的路径 * */
	private Set<String> apiClasses;
	private Set<String> nativeClass;
	private Map nativeMap;
	
	
	public NativeMethods(String applicatoinClassPath){
		this.applicatoinClassPath = applicatoinClassPath;
		this.apiClasses = new LinkedHashSet<String>();
		this.nativeMap = new HashMap(); 
	}
	
	private void setapiClasses(){
		for (String clzName: SourceLocator.v().getClassesUnder(applicatoinClassPath)) {
            //System.out.printf("api class: %s\n", clzName);
            apiClasses.add(clzName);
        }
		
		//loadAppClasses(apiClasses,false);
	}
	public Set<String> getapiClasses() {
        return apiClasses;
    }
	private void setOptions() {
		 soot.options.Options.v().set_keep_line_number(true);
		 soot.options.Options.v().set_whole_program(true);
        // LWG
		 soot.options.Options.v().setPhaseOption("jb", "use-original-names:true");
		 soot.options.Options.v().setPhaseOption("cg", "verbose:false");
		 soot.options.Options.v().setPhaseOption("cg", "trim-clinit:true");
		 //soot.options.Options.v().setPhaseOption("jb.tr", "ignore-wrong-staticness:true");
		 		
		 soot.options.Options.v().set_src_prec(Options.src_prec_java);
		 soot.options.Options.v().set_prepend_classpath(true);
		 		
		 // don't optimize the program 
		 soot.options.Options.v().setPhaseOption("wjop", "enabled:false");
		 // allow for the absence of some classes
		 soot.options.Options.v().set_allow_phantom_refs(true);
		 
		 
	}
	private  void setSootClassPath() {
		//String apiPath = DSHome + File.separator + apiDir;
		StringBuffer cp = new StringBuffer();
		//cp.append(".");
		//cp.append(File.pathSeparator + applicatoinClassPath);
		//add the api modeling directory first so we can load modeling classes
		//cp.append("/media/myw/Study/Master/Research/DroidSafe/droidsafe-src-master/android-lib/droidsafe-api-model.jar");
		//cp.append(File.pathSeparator+"/media/myw/Study/Master/Research/DroidSafe/droidsafe-src-master/bin/droidsafe-core.jar");
		cp.append(File.pathSeparator+"/home/myw/Android/Sdk/platforms/android-26/android.jar");
		cp.append(File.pathSeparator+"/usr/lib/jvm/jdk1.7.0_79/jre/lib/rt.jar"+File.pathSeparator+"/usr/lib/jvm/jdk1.7.0_79/jre/lib/jce.jar");
		//cp.append(File.pathSeparator+"/usr/lib/jvm/jdk1.7.0_79/jre/lib/rt.jar");
		//System.out.println("soot.class.path:\n"+cp.toString());
		
        System.setProperty("soot.class.path", cp.toString());
	}
	public Set<String> loadAppClasses( boolean shouldReplace) {
	    Set<String> notLoaded = new HashSet<String>();
		//load the application classes and set them as app classes
	    for (String clzName: SourceLocator.v().getClassesUnder(applicatoinClassPath)) {
	    	apiClasses.add(clzName);
		    //if we don't want to replace and this class is already defined, then skip
		    if (!shouldReplace && Scene.v().containsClass(clzName) && Scene.v().getSootClass(clzName).isApplicationClass()) {
		    	//System.out.printf("Not loading %s, already loaded\n", clz);
		        notLoaded.add(clzName);
		        continue;
		    }
		    
			//Scene.v().loadClassAndSupport(clz).setApplicationClass();
			System.out.println(clzName);
			Scene.v().loadClass(clzName, SootClass.BODIES).setApplicationClass();;
			//System.out.printf("Loading class as application class: %s", clz);
		}
		System.out.printf("not loaded: %d\n", notLoaded.size());
		return notLoaded;
	}
	/*public void loadClasses(String filePath) {
        
        Set<String> set =  readMethods(filePath);
        for (String reviewd : set) {
        	apiClasses.remove(reviewd);
        }
    }*/
	public void readMethods(String filePath){
		Set<String> nativeMethod = readNativeTxt.readFile(filePath);
		//Set<String> d =  new LinkedHashSet<String>();
		//Set<String> s =  new LinkedHashSet<String>();
		String fileName = "/media/myw/Study/Master/Research/DroidSafe/运行日志/android8_re.txt";
		int n = 0;
		int num = 0;
		int no = 0,immutableNum =0;
		for (SootClass clz : Scene.v().getApplicationClasses()) {
			if(Modifier.isInterface(clz.getModifiers())){
				continue;
			}
			if(Modifier.isAbstract(clz.getModifiers())){
				continue;
			}
			int i=0;
			 for(SootField fld : clz.getFields()){
				 if(Modifier.isFinal( fld.getModifiers())){i++;}
			 }
			 if(Modifier.isFinal( clz.getModifiers()) && ( i == clz.getFields().size())){
				 //System.out.printf("%s是immutable类\n",clz.getName());
				AppendContentToFile.append("/media/myw/Study/Master/Research/DroidSafe/script/newImmutableType.txt",clz.getName());
				immutableNum++;
			}
			 
			n++;
			String className = clz.getName();
			if (nativeClass.contains(className)){
				nativeClass.remove(className);
				//d.add(className);	
			}
			//System.out.println(className);
			
			if (clz.getMethods().size() == 0) {no += 1;	}
			else{
				for (SootMethod method : clz.getMethods()) {
					 int modifier = method.getModifiers();
					 //System.out.println(modifier);
					 if( Modifier.isNative(modifier)){
						 //if(method.isStatic()){
							 String sig = method.getSignature(clz, method.getName(), method.getParameterTypes(), method.getReturnType());
							 AppendContentToFile.append("/media/myw/Study/Master/Research/DroidSafe/运行日志/android8_nativeMth", sig);
							 num += 1;
						// }
						 //if(nativeMethod.contains(sig)){ 
							 //s.add(className);
//							 if(method.isConcrete()){
//								 if (method.hasActiveBody()){
//									 Body mbody = method.getActiveBody();
//									 System.out.println(mbody.toString()); 
///								 }
//								 else{
//									 System.out.println(method.retrieveActiveBody().toString());
//									 AppendContentToFile.method3(fileName, method.retrieveActiveBody().toString());
//								 }
//							 }
						 //} 
					 }
		         }
			}
		}
		//System.out.println("the sum iof classes that Do ot have any methods: "+ no);
		//System.out.println("application class num: "+ n);
		
		//System.out.printf("not private native method number: %d\n", num);
		System.out.printf("immutable type num: %d\n", immutableNum);
	}
	public void  setNativeclass(String filePath){
		
		this.nativeMap.putAll(readNativeTxt.readFileByLines(filePath));		
		nativeClass = this.nativeMap.keySet();
	}
	
	public Set<String> getNativeClass(){
		return nativeClass;
	}
	
	public void statistic(){
		int sum =0;
		System.out.println(nativeMap.size());
		for(String clz: nativeClass){
			Object list = nativeMap.get(clz);
			if(list instanceof List){
				sum +=((List) list).size();
				System.out.println(clz+"------"+((List) list).size());
			}	
		}
		System.out.println("unload method in native.txt: "+sum);
	}
	public Set<String> getApiClasses(){return apiClasses;}
	public static void main(String[] arg){
		soot.G.reset();
		
		NativeMethods s = new NativeMethods("/home/myw/Android/Sdk/sources/android-26");
		s.setNativeclass("/media/myw/Study/Master/Research/DroidSafe/droidsafe-src-master/config-files/native.txt");
		s.setSootClassPath();
		s.setOptions();
		
		//Scene.v().loadNecessaryClasses();
		//s.setapiClasses();
		System.out.println(s.getapiClasses().size());
		s.loadAppClasses(false);
		System.out.printf("api directory contain %d classes\n",s.getApiClasses().size());
		s.readMethods("/media/myw/Study/Master/Research/DroidSafe/droidsafe-src-master/config-files/native.txt");
		
		//System.out.println(s.getapiClasses().size());
		
		System.out.println(s.getNativeClass().size());
		s.statistic();
		//System.out.println(s.getapiClasses().toString());
		//s.readMap("/home/myw/droidsafe/droidsafe-src-master/config-files/native.txt");
		
	}
}
