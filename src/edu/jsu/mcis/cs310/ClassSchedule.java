package edu.jsu.mcis.cs310;

import com.github.cliftonlabs.json_simple.*;
import com.opencsv.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class ClassSchedule {
    
    private final String CSV_FILENAME = "jsu_sp24_v1.csv";
    private final String JSON_FILENAME = "jsu_sp24_v1.json";
    
    private final String CRN_COL_HEADER = "crn";
    private final String SUBJECT_COL_HEADER = "subject";
    private final String NUM_COL_HEADER = "num";
    private final String DESCRIPTION_COL_HEADER = "description";
    private final String SECTION_COL_HEADER = "section";
    private final String TYPE_COL_HEADER = "type";
    private final String CREDITS_COL_HEADER = "credits";
    private final String START_COL_HEADER = "start";
    private final String END_COL_HEADER = "end";
    private final String DAYS_COL_HEADER = "days";
    private final String WHERE_COL_HEADER = "where";
    private final String SCHEDULE_COL_HEADER = "schedule";
    private final String INSTRUCTOR_COL_HEADER = "instructor";
    private final String SUBJECTID_COL_HEADER = "subjectid";
    private final String COURSE = "course";
    private final String SCHEDULE_TYPE = "scheduletype";
    
    public String convertCsvToJsonString(List<String[]> csv) {
        
        // Prepare top-level JSON container 
        
        JsonObject json = new JsonObject();
        
        // Prepare JSON sub-containers
        
        JsonObject scheduletype = new JsonObject();
        JsonObject subject = new JsonObject();
        JsonObject course = new JsonObject();
        JsonArray section = new JsonArray();
        
        String jsonString = null;
        
        //looping through the rows of CSV
        Iterator<String[]> iterator = csv.iterator();
        //taking the header labels from the csv
        String[] headerRow = iterator.next();
        
        //hashmap to keep track of our headers and positions
        HashMap<String, Integer> headers = new HashMap<>();
        
        for (int i = 0; i < headerRow.length; ++i) {
            headers.put(headerRow[i], i);
        }
        
        while (iterator.hasNext()) {
            
            // Get next CSV row
            
            String[] row = iterator.next();
            
            
            String numOriginalField = row[headers.get(NUM_COL_HEADER)];
            String subjectField = row[headers.get(SUBJECT_COL_HEADER)];
            String typeField = row[headers.get(TYPE_COL_HEADER)];
            String scheduleField = row[headers.get(SCHEDULE_COL_HEADER)];
            String crnField = row[headers.get(CRN_COL_HEADER)]; //needs to be int 
            String sectionField = row[headers.get(SECTION_COL_HEADER)];
            String startTime = row[headers.get(START_COL_HEADER)];
            String endTime = row[headers.get(END_COL_HEADER)];
            String days = row[headers.get(DAYS_COL_HEADER)];
            String where = row[headers.get(WHERE_COL_HEADER)];
            String[] instructors = row[headers.get(INSTRUCTOR_COL_HEADER)].split(",");
          
            String[] numArray = numOriginalField.split(" ");
            String subjectidField = numArray[0];
            String numField = numArray[1];
            
             // Add "subjectid" and "subject" to "subject" container as key/value pair
            subject.put(subjectidField, subjectField);
         
            // Add "type" and "schedule" to "scheduletype" container as key/value pair
            
            scheduletype.put(typeField, scheduleField);
            
            // Create course element map; add "subjectid", "num", "description", and "credits" (as int!)
            
            JsonObject courseElement = new JsonObject();
            
            courseElement.put(SUBJECTID_COL_HEADER, subjectidField);
            courseElement.put(NUM_COL_HEADER, numField);
            courseElement.put(DESCRIPTION_COL_HEADER, row[headers.get(DESCRIPTION_COL_HEADER)]);
            courseElement.put(CREDITS_COL_HEADER, Integer.valueOf(row[headers.get(CREDITS_COL_HEADER)]));
            
            // Add to "course" container with the full course number (subjectid and num) as the key
            
            course.put(numOriginalField, courseElement);
            
            // Create section element map; add "crn", "subjectid", "num", "section", "type", start/end times,
            // "days", and "where" fields.  Create a new JsonArray; split instructor list by comma, add each
            // instructor to new JsonArray in original order, and add JsonArray to section element map with
            // key name "instructor".  Finally, add section element map to end of "section" JsonArray.
            
            JsonObject sectionElement = new JsonObject();
            
            sectionElement.put(CRN_COL_HEADER, Integer.valueOf(row[headers.get(CRN_COL_HEADER)]));
            sectionElement.put(SUBJECTID_COL_HEADER, subjectidField);
            sectionElement.put(NUM_COL_HEADER, numField);
            sectionElement.put(SECTION_COL_HEADER, sectionField);
            sectionElement.put(TYPE_COL_HEADER, typeField);
            sectionElement.put(START_COL_HEADER, startTime);
            sectionElement.put(END_COL_HEADER, endTime);
            sectionElement.put(DAYS_COL_HEADER, days);
            sectionElement.put(WHERE_COL_HEADER, where);
            
            
            //taking our instructors, after they've been split, trimming the white space, and then adding them to 
            //our instructorsList
            List<String> instructorsList = new ArrayList<>();
           
           for(String instructor : instructors){
               instructorsList.add(instructor.trim()); //trim is going to let us get rid of the white space
           }
               
           sectionElement.put(INSTRUCTOR_COL_HEADER, instructorsList);
           
           section.add(sectionElement);
            
       //adding to our largest container, the json object
       json.put("subject", subject);
       json.put("scheduletype", scheduletype);
       json.put("course", course);
       json.put("section", section);
        
       
     }
       jsonString = Jsoner.serialize(json);
       return jsonString;
        
    } 

     public String convertJsonToCsvString(JsonObject jsonData) {

    //create a copy of the input JsonObject
    JsonObject dataObject = new JsonObject(jsonData);

    //taking out our objects and array from the main object so we can manipulate it: 
    JsonObject scheduleTypes = (JsonObject) dataObject.get("scheduletype");
    JsonObject subjects = (JsonObject) dataObject.get("subject");
    JsonObject courses = (JsonObject) dataObject.get("course");
    JsonArray sections = (JsonArray) dataObject.get("section");

    //create a list to store CSV lines, starting with the header:
    List<String[]> csvLines = new ArrayList<>();

    //define the CSV header:
    String[] csvHeader = {CRN_COL_HEADER, SUBJECT_COL_HEADER, NUM_COL_HEADER, DESCRIPTION_COL_HEADER, SECTION_COL_HEADER, TYPE_COL_HEADER, CREDITS_COL_HEADER,
                          START_COL_HEADER, END_COL_HEADER, DAYS_COL_HEADER, WHERE_COL_HEADER, SCHEDULE_COL_HEADER, INSTRUCTOR_COL_HEADER};
    csvLines.add(csvHeader);

    //iterate through each section in the sections array:
    for (int i = 0; i < sections.size(); i++) {
        //getting the current section of our JsonObject
        JsonObject currentSection = (JsonObject) sections.get(i);

        //take instructor names from the instructor array and concatenate them into one string
        JsonArray instructorArray = (JsonArray) currentSection.get(INSTRUCTOR_COL_HEADER);
        String[] instructorNames = instructorArray.toArray(new String[0]); 
        String instructors = String.join(", ", instructorNames); //this (String.join) is putting our instructors back together and specifying that they are separated by a comma!
        
        //putting our details into a hashmap to produce a single csv line to go in as a row: 
        HashMap courseDetails = (HashMap) courses.get((currentSection.get(SUBJECTID_COL_HEADER) + " " + currentSection.get(NUM_COL_HEADER)));
        String[] csvLine = {currentSection.get(CRN_COL_HEADER).toString(), subjects.get(currentSection.get(SUBJECTID_COL_HEADER)).toString(),
        (currentSection.get(SUBJECTID_COL_HEADER) + " " + currentSection.get(NUM_COL_HEADER)), courseDetails.get(DESCRIPTION_COL_HEADER).toString(),
        currentSection.get(SECTION_COL_HEADER).toString(),currentSection.get(TYPE_COL_HEADER).toString(),courseDetails.get(CREDITS_COL_HEADER).toString(),
        currentSection.get(START_COL_HEADER).toString(),currentSection.get(END_COL_HEADER).toString(),currentSection.get(DAYS_COL_HEADER).toString(),
        currentSection.get(WHERE_COL_HEADER).toString(),scheduleTypes.get(currentSection.get(TYPE_COL_HEADER).toString()).toString(),instructors};
        //add the CSV line to the list
        csvLines.add(csvLine);
    }
    
    //we set up our StringWriter and CSVWriters: 
    StringWriter csvStringWriter = new StringWriter();
    CSVWriter csvWriter = new CSVWriter(csvStringWriter, '\t', '"', '\\', "\n");
    
    //write all CSV lines to the StringWriter
    csvWriter.writeAll(csvLines);

    //return the CSV data as a String
    return csvStringWriter.toString();
}
    
    public JsonObject getJson() {
        return getJson(getInputFileData(JSON_FILENAME));
    }

    public JsonObject getJson(String input) {
        JsonObject json = null;
        try {
            json = (JsonObject) Jsoner.deserialize(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    public List<String[]> getCsv() {
        return getCsv(getInputFileData(CSV_FILENAME));
    }

    public List<String[]> getCsv(String input) {
        List<String[]> csv = null;
        try {
            CSVReader reader = new CSVReaderBuilder(new StringReader(input))
                    .withCSVParser(new CSVParserBuilder().withSeparator('\t').build())
                    .build();
            csv = reader.readAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return csv;
    }

    public String getCsvString(List<String[]> csv) {
        StringWriter writer = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(writer, '\t', '"', '\\', "\n");
        csvWriter.writeAll(csv);
        return writer.toString();
    }

    private String getInputFileData(String filename) {
        StringBuilder buffer = new StringBuilder();
        String line;
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(loader.getResourceAsStream("resources" + File.separator + filename)));
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append('\n');
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buffer.toString();
    }
}
