package com.ca.uim.git.restcalls;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.json.simple.parser.ParseException;

import com.ca.uim.git.datastore.BranchStore;
import com.ca.uim.git.model.Branch;
import com.ca.uim.git.model.Organization;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException, ParseException, InterruptedException, ExecutionException, java.text.ParseException 
    {
        GitRest gitRest = new GitRest();
        Organization org = gitRest.gitRestCall(args[0],args[1],args[2],Integer.valueOf(args[3]));
        BranchStore branchStore= new BranchStore();
        branchStore.storeData(org);
    }
}
