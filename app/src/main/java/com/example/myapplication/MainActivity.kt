package com.example.myapplication

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.apollographql.apollo.rx2.Rx2Apollo
import com.apollographql.apollo.sample.FirstQuery
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.checkbox_response.view.*
import kotlinx.android.synthetic.main.question.view.*
import kotlinx.android.synthetic.main.radio_group.view.*
import kotlinx.android.synthetic.main.section.view.*
import kotlinx.android.synthetic.main.segment.view.*
import kotlinx.android.synthetic.main.text_response.view.*
import kotlinx.android.synthetic.main.textarea_response.view.*
import android.widget.RadioButton

class MainActivity : AppCompatActivity() {

    //private lateinit var apolloClient: MyApolloClient
    private lateinit var parentLinearLayout: LinearLayout
    private var firstQuery = FirstQuery.builder().build()

    val relations: MutableList<Relation> = mutableListOf()

    //Data maps
    val sections = mutableMapOf<Int?,Indication?>()
    val segments = mutableMapOf<Int?,Indication?>()
    val questions = mutableMapOf<Int?,Indication?>()
    val responses = mutableMapOf<Int?,Response?>()

    //Flags
    var index = 0
    var scFlag: Int? = null
    var sgFlag: Int? = null
    var qFlag: Int? = null
    var radioBlockFlag: Boolean = false

    //Receivers
    var sec: Indication? = null
    var seg: Indication? = null
    var ques: Indication? = null
    var res: Response? = null

    lateinit var radioGroup: View


    override fun onCreate(savedInstanceState: Bundle?) {
        //Boilerplate code
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //You need this for the dynamic views
        parentLinearLayout = findViewById(R.id.parent_linear_layout)

        //Start
        getAllFormOutline()
    }

    private fun getAllFormOutline(){

        val apolloCall = MyApolloClient.getMyApolloClient().query(firstQuery)

        val observable1 = Rx2Apollo
            .from(apolloCall)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                //PRINT FORM TITLE
                val formTitle = it.data()?.formOutline()?.description()
                println("\nTítulo del Formulario: $formTitle")

                updateMyTextView(formTitle)

                it.data()?.formOutline()?.formsSegments()?.forEach {
                    //SAVE THE SECTIONS
                    val scId = it.formSectionIdObject()?.id()
                    val scPos = it.formSectionIdObject()?.pos()
                    val scTitle = it.formSectionIdObject()?.description_title()
                    val scText = it.formSectionIdObject()?.description_text()

                    saveInSections(scId, scPos, scTitle, scText)

                    //SAVE THE SEGMENTS
                    val sgId = it.id()
                    val sgPos = it.pos()
                    val sgTitle = it.description_title()
                    val sgText = it.description_text()

                    saveInSegments(sgId, sgPos, sgTitle, sgText)

                    it.responsesTypes()?.forEach {
                        //SAVE THE QUESTIONS
                        val qId = it.formLabelIdObject()?.id()
                        val qPos = it.formLabelIdObject()?.pos()
                        val qDesc = it.formLabelIdObject()?.description()

                        saveInQuestions(qId, qPos, qDesc)

                        //SAVE THE RESPONSES
                        val rId = it.id()
                        val rPos = it.pos()
                        val rDesc = it.description()
                        val rTypeId = it.optionTypeIdObject()?.id()
                        val rTypeDesc = it.optionTypeIdObject()?.description()

                        saveInResponses(rId, rPos, rDesc, rTypeId, rTypeDesc)

                        saveInRelations(scId, sgId, qId, rId)
                    }
                }

                showForm()

            },{
                it.printStackTrace()
            })
    }

    private fun updateMyTextView(text: String?){
        title_textview.text = text
    }

    private fun saveInRelations(scId: Int?, sgId: Int?, qId: Int?, rId: Int?){
        //println("  SECTION ID/POS: $id/$pos - TITLE: $title - TEXT: $text")
        relations.add(Relation(scId, sgId, qId, rId))
    }

    private fun saveInSections(id: Int?, pos: Int?, title: String?, text: String?){
        //println("  SECTION ID/POS: $id/$pos - TITLE: $title - TEXT: $text")
        sections[id] = Indication(id, pos, title, text)

    }

    private fun saveInSegments(id: Int?, pos: Int?, title: String?, text: String?){
        //println("  SEGMENT ID/POS: $id/$pos - TITLE: $title - TEXT: $text")
        segments[id] = Indication(id, pos, title, text)
    }

    private fun saveInQuestions(id: Int?,  pos: Int?, desc: String?){
        //println("    QUESTION ID: $id - QUESTION: $desc")
        questions[id] = Indication(id, pos, desc)
    }

    private fun saveInResponses(id: Int?, pos: Int?, desc: String?, typeId: Int?, typeDesc: String?) {
        //println("      ANSWER ID/POS: $id/$pos - ANSWER: $desc - TYPE: $type ($typedesc)")
        responses[id] = Response(id, pos, desc, typeId, typeDesc)
    }

    private fun showForm(){
        var i = 0
        relations.forEach{

            if(scFlag != it.sectionId) {

                //Set new flags
                scFlag = it.sectionId
                sgFlag = it.segmentId
                qFlag = it.questionId

                //Retrieve data
                sec = sections[it.sectionId]
                seg = segments[it.segmentId]
                ques = questions[it.questionId]
                res = responses[it.responseId]

                checkRadioGroup()

                //Create the views
                addSection(sec?.title)
                addSection(sec?.text)

                addSegment(seg?.title)
                addSegment(seg?.text)

                createQuestion(ques?.title)
                createResponse(res?.text, res?.typeId)

                //Print data
                println("Section: " +
                //        "${sec?.id}, " +
                //        "${sec?.position}, " +
                        "${sec?.title}, " +
                        "${sec?.text}"
                )
                println("   Segment: " +
                //        "${seg?.id}, " +
                //        "${seg?.position}, " +
                        "${seg?.title}, " +
                        "${seg?.text}"
                )
                println("       Question: " +
                //        "${ques?.id}, " +
                //        "${ques?.position}, " +
                        "${ques?.title}"
                )
                println("           Response: " +
                //        "${res?.id}, " +
                //        "${res?.position}, " +
                        "${res?.text}, " +
                //        "${res?.typeId}, " +
                        "${res?.typeDescription}"
                )
            }
            else{
                if (sgFlag != it.segmentId) {

                    //Set new flags
                    sgFlag = it.segmentId
                    qFlag = it.questionId

                    //Retrieve data
                    seg = segments[it.segmentId]
                    ques = questions[it.questionId]
                    res = responses[it.responseId]

                    checkRadioGroup()

                    //Create the views
                    addSegment(seg?.title)
                    addSegment(seg?.text)

                    createQuestion(ques?.title)
                    createResponse(res?.text, res?.typeId)

                    //Print data
                    println("   Segment: " +
                    //        "${seg?.id}, " +
                    //        "${seg?.position}, " +
                            "${seg?.title}, " +
                            "${seg?.text}"
                    )
                    println("       Question: " +
                    //        "${ques?.id}, " +
                    //        "${ques?.position}, " +
                            "${ques?.title}"
                    )
                    println("           Response: " +
                    //        "${res?.id}, " +
                    //        "${res?.position}, " +
                            "${res?.text}, " +
                    //        "${res?.typeId}, " +
                            "${res?.typeDescription}"
                    )
                }
                else{
                    if(qFlag != it.questionId){

                        //Set flag
                        qFlag = it.questionId

                        //Retrieve data
                        ques = questions[it.questionId]
                        res = responses[it.responseId]

                        checkRadioGroup()

                        //Create the views
                        createQuestion(ques?.title)
                        createResponse(res?.text, res?.typeId)

                        //Print data
                        println("       Question: " +
                        //        "${ques?.id}, " +
                        //        "${ques?.position}, " +
                                "${ques?.title}"
                        )
                        println("           Response: " +
                        //        "${res?.id}, " +
                        //        "${res?.position}, " +
                                "${res?.text}, " +
                        //        "${res?.typeId}, " +
                                "${res?.typeDescription}"
                        )
                    }
                    else{
                        //Retrieve data
                        res = responses[it.responseId]

                        //Crete the views
                        createResponse(res?.text, res?.typeId)

                        //Print data
                        println("           Response: " +
                        //        "${res?.id}, " +
                        //        "${res?.position}, " +
                                "${res?.text}, " +
                        //        "${res?.typeId}, " +
                                "${res?.typeDescription}"
                        )
                    }
                }
            }
            i++
        }
    }

    fun onFinish(view: View){
        // submit/send data
    }

    fun hideView(view: View){
        parentLinearLayout.removeView(view.parent as View)
    }

    fun addSection(text: String?){
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowView: View = inflater.inflate(R.layout.section, null)
        parentLinearLayout.addView(rowView, parentLinearLayout.childCount - 1)
        rowView.section_textView.text = text
    }

    fun addSegment(text: String?){
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowView: View = inflater.inflate(R.layout.segment, null)
        parentLinearLayout.addView(rowView, parentLinearLayout.childCount - 1)
        rowView.segment_textView.text = text
    }

    fun checkRadioGroup(){
        if (radioBlockFlag) {  //this means a radio group is open, so let's close it
            parentLinearLayout.addView(radioGroup, parentLinearLayout.childCount - 1)
            radioBlockFlag = false
        }
    }

    fun createQuestion(text: String?){
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowView: View = inflater.inflate(R.layout.question, null)
        parentLinearLayout.addView(rowView, parentLinearLayout.childCount - 1)
        rowView.question_textView?.text = text
    }

/*    fun createIndication(text: String?){
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowView: View = inflater.inflate(R.layout.indication, null)
        parentLinearLayout.addView(rowView, parentLinearLayout.childCount - 1)
        rowView.indication_textView?.text = text
    }*/

    fun createResponse(text: String?, typeId: Int?){
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        // Switch case (typeId):
        //  5: Check
        //  6: Radio
        //  7: Text
        //  8: TextArea (multiline)
        //  9: Date
        // 10: DateTime (hora)
        // 11: Int

        if(typeId == 6){ //Opens a group of radio buttons

            if (!radioBlockFlag) {

                //Create RadioGroup
                radioGroup = inflater.inflate(R.layout.radio_group, null)

                //Activate flag (this states that a radio group is open)
                radioBlockFlag = true
            }

            //Create RadioButton
            val radioButton = RadioButton(this)
            radioButton.text = text
            //Add radioButton to radioGroup
            radioGroup.radioGroup.addView(radioButton)
        }
        else {
            if (radioBlockFlag) {  //this means a radio group is open, so let's close it
                checkRadioGroup()
            }

            val rowView: View? = when (typeId) {
                5 -> inflater.inflate(R.layout.checkbox_response, null)
                7 -> inflater.inflate(R.layout.text_response, null)
                8 -> inflater.inflate(R.layout.textarea_response, null)
                9 -> inflater.inflate(R.layout.date_response, null)
                10 -> inflater.inflate(R.layout.datetime_response, null)
                else -> inflater.inflate(R.layout.int_response, null)
            }

            parentLinearLayout.addView(rowView, parentLinearLayout.childCount - 1)


            when(typeId){
                5 -> rowView?.checkBoxResponse?.text = text
                //6 -> rowView.radioButtonResponse?.text = text
                7 -> rowView?.textResponse_textView?.text = text
                8 -> rowView?.textAreaResponse_textView?.text = text
            }
        }

    }

    //PROBABLY DEPRECATED. KEPT AS REFERENCE.

/*    fun addQuestion(rows: Int){
        var n = rows
        while (n > 0){
            createQuestion("DEFAULT")
            n--
        }
    }*/
}

