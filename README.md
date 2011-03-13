
# form-dot-clj #

A library for handling the display and validation of HTML forms. The library is designed to be used with Ring and supports XSS prevention, HTML5 forms, javascript validation and plain HTML.

This and other form validation libraries are available on [clojars](http://clojars.org/search?q=form) for use with Leiningen/Maven
     :dependencies [[uk.org.alienscience/form-dot-clj "0.0.4"]]

## Example ##

    (def-field username
      [:maxlength 20]
      [:pattern "[A-Za-z0-9]+" "Only alphanumeric characters please"])
      
    (def-field email-address
      [:email "Sorry, that style of email address is not supported"])

    (def-form example 
      {:size 20 :required "Please fill this in"}
      :username (textbox username)
      :email    (textbox email-address))

    ;; Use the following function to show the form when generating html
    (show-controls example params errors)

    ;; Use the following function to validate a post of this form
    (on-post example params success-fn failure-fn)
         

## Introduction ##

Generating and checking HTML forms is boring and its easy to make small mistakes. For example, hit submit on an empty form and watch how too many web pages display unsuitable errors or even fail to spot input is missing.

 Using Javascript or HTML5 form handling improves the response time for users but means writing more code. Even when using client side validation, server checks are still needed to protect against scripts.

Form-dot-clj is a Clojure library that attempts to remove the drudgery from form display, validation, type conversion and error reporting. The library is extendable and an example is included where controls based on [jquery tools](http://flowplayer.org/tools/release-notes/index.html#form) are used to provide HTML5, javascript and server side validation from the same declarations.

This library should be usable with any HTML templating system and also provides fine grained control of the HTML that is generated. The examples directory contains a file `signup-doc.clj` that shows different levels of HTML generation.

## Overview ##

This library takes the following approaches to validation:
- It assumes the Ring request has a parameter map containing string keys. Anything with a string key has not been validated. When data has been validated, it has :keyword keys.
- The validation is declarative so that it can be used to generate Javascript or HTML5 form checks if needed.
- Although its straightforward to lookup the correct regular expressions to check email addresses and prevent various attacks, not everybody does this. Form-dot-clj comes with various, optional, pre-defined checks and attempts to follow best practice.
- Posted data comes in as strings. The library allows for, optional, type conversion. Validation can be done on both the string and on the type converted data.
- All validations end up in globally accessible def forms. This is to encourage the reuse of validations across namespaces so that, for example, a userid on one form is validated identically to a userid on another. 
- You can, optionally, use functions to validate form posts and these functions can validate multiple fields together.
- If you want to follow the principle of "Do Not Repeat Yourself" you can, optionally, use Form-dot-clj to build HTML forms. So for instance, maxlength only has to be specified once for a field and it will be used for validation and in the resulting HTML.
- The library includes, optional, XSS detection. If a form submission contains an XSS attack then it is prudent not to accept any of the data in the submission.
- The library comes with a helper function `on-post` that handles the common submit, validate, display errors and commit workflow that occurs with when handling HTML forms. Use of this function is optional.

## Walkthrough ##

Some websites need a signup form to register users. This is how we would use form-dot-clj to do it.

    (ns signup
      "Minimal signup form"
      (:use form-dot-clj.core)
      (:use form-dot-clj.html-controls))

The declaration above pulls in form-dot-clj and will give us access to HTML controls. If we wanted client side valiation using jquery tools we would write:

    (ns signup
      "Minimal signup form"
      (:use form-dot-clj.core)
      (:use form-dot-clj.jquery-tools))

Form-dot-clj uses controls and fields. A control is something that appears on a HTML page. A field describes how data is validated. Form-dot-clj separates these concepts as the same field may be attached to different controls.

    (def-field username
      [:maxlength 20]
      [:pattern "[a-zA-Z]+" "Only alphanumeric characters please"]
      [:no-match #"(?i)(root|admin)" "Sorry that username is reserved"])

The declaration above creates a var called 'username' that is defined with a series of declarations in the format `[:check arg1 ... argn]`. By default, the field is assumed to be a string although other types are available. 

`[:maxlength 20]` sets the maximum length of the field. This declaration is used to produce HTML that limits the number of characters that can be input. The declaration is also used to build a check on the server.

`[:pattern "[a-zA-Z]+" "Error message"]` sets a, javascript compatible, regular expression that the text must fully match to be valid. This check also introduces an error message that will be displayed when the check fails. The check will be made on the server and optionally on the client if, for example, jquery tools is being used.

`[:no-match #"(?i)(root|admin)" "Error message"]` creates a check that will only be on the server. It uses a clojure regular expression to make sure that the username doesn't contain unwanted patterns.

The other fields on the form don't require as many checks:

    (def-field name     [:maxlength 50])
    (def-field email    [:email "Is that really an email address?"])
    (def-field password [:maxlength 50])
             
We can now define the form that will use these fields.

    (def-form signup
      {:size 20 :required "Required" :check-fns [confirm-password]}
      :username         (textbox username)
      :name             (textbox name)
      :email            (textbox email)
      :password         (textbox password {:type "password"})
      :confirm-password (textbox password {:type "password"}))

The form declaration creates a var called 'signup' that holds details about the form. The map following the name contains settings and defaults for the form.

      {:size 20 :required "Required" :check-fns [confirm-password]}

The HTML controls that use a size attribute will have that attribute set to 20 by default. All the controls will be required by default. If a textbox is not filled in, an error message saying "Required" will be displayed. In addition to checks created automatically from the fields, we have specified our own checking function by setting `:check-fns`.

The controls for the form are each given with a key e.g:

    :username         (textbox username)
    :password         (textbox password {:type "password"})
    :confirm-password (textbox password {:type "password"})

The password box has the HTML type attribute set to "password". We want password and confirm-password to be the same value and we can enforce this by writing our own checking function:

    (defn confirm-password
      "Makes sure password and confirmation are the same"
      [params errors]
      (if (and (not (contains? errors :password))
               (not (= (params "confirm-password")
                       (params "password"))))
        {:password "Password and confirmation are not the same"}))

The checking function is passed a map of the parameters and a map of all the errors that have occured so far. The error map can be used to see if this check is needed at all - for instance we wouldn't want to do an expensive database check on a field if the field was already invalid. Errors are returned in a map with the format `:control-key "Error message` to indicate which control has an error.

We can now write a function to display the form. 

    (defn show-form
      "The easy way to display a form"
      [params errors]
      (html
        [:form {:action "/signup" :method "post"}
           (show-controls signup params errors)
           (default-submit "Sign Up")]))

The example above uses [Hiccup](http://github.com/weavejester/hiccup) for HTML generation but other libraries can be used. In the examples directory is a demonstration of form-dot-clj being used with [Enlive](http://cgrand.github.com/enlive/). The function `(show-controls form params errors)` returns the controls as HTML. The function `(default-submit "Label")` returns a HTML submit button with similar formatting. 

The HTML that is returned can be styled using CSS. However, if more control over the HTML is needed then it is possible to change the way each control is formatted or have full control over the form layout.

Assuming we have a function that does the signup called `do-signup`, we need to write code to do validation and error display when the form is posted.

    (defn signup-post
      "The easy way to handle a post"
      [params]
      (on-post signup params do-signup show-form))

The `on-post` function takes as arguments the form to be validated, the posted parameters, a function to call on success and a function to display the form when errors occur.
      
If more control is needed when a form is posted then lowerlevel functions are available to handle the post in more detail. The file `signup-doc.clj` in the examples directory shows how this is done.

The on-post function needs to be called during a post, for instance, when using [Compojure](http://github.com/weavejester/compojure) to do HTTP routing:

    (defroutes myroutes
       (GET "/signup" [] (show-form {} {}))
       (POST "/signup" {params params}
          (signup-post params)))

To see how this fits together, runnable web applications are given in the 'examples' directory. It is recommended that you look at these before the more detailed documentation below.
          
## Validation ##

The following validation tags are available for use with `def-field`

    [:maxlength x]
Sets the maximum character length for a field *(client,server)*.

    [:minlength x]
Sets the minimum character length for a field *(server)*.

    [:pattern pat error-message]
A string must fully match the javascript regular expression, pat, (given as a string) or error-message will be returned *(client,server)*.

    [:match re error-message]
A string must match the given Clojure regular expression or error-message will be displayed *(server)*.

    [:no-match re error-message]
A string must not match the given Clojure regular expression or error-message will be displayed *(server)*.

    [:integer min max error-message]
A string field is converted into an integer. The integer is bounded by max and min. If the limits are exceeded or the type conversion fails, error-message is displayed *(client, server)*.

    [:float min max error-message]
A string field is converted into a floating point value. The number is bounded by max and min. If the limits are exceeded or the type conversion fails, error-message is displayed *(client, server)*.

    [:date min max error-message]
An string field holding a date in the format "YYYY-MM-DD" is converted into a date object as used by the [clj-time library](http://github.com/clj-sys/clj-time) *(client, server)*. 

    [:url error-message]
Checks that a string appears to be a valid URL *(client, server)*. 

    [:boolean]
Extracts a boolean from a string. A missing field, empty strings, and case insensitive matches to "no" or "false" are converted to `false`. Anything else is converted `true` *(server)*.

    [:xss->element]

Makes a field safe to be placed in a HTML element. For more information see [the OWASP cheat sheet](http://www.owasp.org/index.php/XSS_%28Cross_Site_Scripting%29_Prevention_Cheat_Sheet#RULE_.231_-_HTML_Escape_Before_Inserting_Untrusted_Data_into_HTML_Element_Content) *(server)*.

    [:xss->attribute]
    
Makes a field safe to put into a HTML attribute. For more information see [the OWASP cheat sheet](http://www.owasp.org/index.php/XSS_%28Cross_Site_Scripting%29_Prevention_Cheat_Sheet#RULE_.232_-_Attribute_Escape_Before_Inserting_Untrusted_Data_into_HTML_Common_Attributes) *(server)*.

    [:xss->js-data]
    
Makes a field safe to put into javascript data. For more information see [the OWASP cheat sheet](http://www.owasp.org/index.php/XSS_%28Cross_Site_Scripting%29_Prevention_Cheat_Sheet#RULE_.233_-_JavaScript_Escape_Before_Inserting_Untrusted_Data_into_HTML_JavaScript_Data_Values) *(server)*.

    [:xss->css]
    
Makes a field safe to put into a css property value. For more information see [the OWASP cheat sheet](http://www.owasp.org/index.php/XSS_%28Cross_Site_Scripting%29_Prevention_Cheat_Sheet#RULE_.234_-_CSS_Escape_Before_Inserting_Untrusted_Data_into_HTML_Style_Property_Values) *(server)*.

    [:xss->url]
    
Makes a field safe to put into a url. For more information see [the OWASP cheat sheet](http://www.owasp.org/index.php/XSS_%28Cross_Site_Scripting%29_Prevention_Cheat_Sheet#RULE_.235_-_URL_Escape_Before_Inserting_Untrusted_Data_into_HTML_URL_Parameter_Values) *(server)*.

## Controls ##

### form-dot-clj.html-controls ###

     control       options
     -------------------------------------------------------------
     textbox       :name :label :size :type :maxlength :required
     selectbox     :fill-keys :name :label :size :fill-fn :required
     radiobutton   :fill-keys :name :label :fill-fn
     checkbox      :name :label :value :required

The options for each control get converted into HTML attributes apart from:

- `:label` Manually sets the label for the control. 
- `:required` Indicates that a field is required and sets the error message for missing data.
- `:fill-fn` Defines a function (with no arguments) that will return a sequence of maps to fill select boxes and radio buttons.
- `:fill-keys` By default, a sequence of maps with the keys `:id` and `:content` are used to fill select boxes and radio buttons. Setting the option `:fill-keys` to a vector such as `[:name :description]` changes the keys.

### form-dot-clj.jquery-tools ###

     control       options
     -------------------------------------------------------------
     textbox       :name :label :size :type :required
     number-input  :name :label :size :required
     date-input    :name :label :size :required :format
     range-input   :name :label :step

Additional options:

- `:format` The format of the date input display e.g "dd mmm yyyy" or "mm/dd/yy" etc.

When using jquery-tools the following function should be called to set data in the HTML header:

#### include-js ####

(form form-id)

Returns the javascript required to activate jquery-tools for the given form variable with the given HTML form-id. 

## API  ##

### def-field ###

([field-name & validation])

*Macro*
Creates a var with the given name to hold a field definition.
e.g

    (def-field username [:maxlength 20])  ;; creates (var username)

### def-form ###

([name options & definition])

*Macro*
Creates a var in the current namespace that contains
the given form definition. Options is a map of key/values that
is merged with the options on each control - this can be used to
set defaults. The following special options also have meaning:
     :check-fns    - a sequence of user defined functions that will
                     be used to check posted values
   e.g. 
    (def-form login
      {:size 20}
      :username (textbox username {:size 30})
      :password (textbox password {:type "password"}))

### default-submit ###

([label])

The default way of displaying a submit button.

### map-controls ###

([form params errors format-fn])

Maps the controls on the given form through the given function.
    (format-fn [label control-html error] ...)

### on-error ###

([errors k error-fn])

Executes error-fn if the given control has an error.

    errors   - the error map
    k        - the key of the control
    error-fn - (fn [error-message] ... )

### on-post ###

([form params success-fn fail-fn])

Function that handles a form post.
Executes success-fn on success, fail-fn on fail.
The success-fn takes a single parameter containing a map of validated
parameters.
The fail-fn is assumed to redisplay the form and has the parameters:
    params  - the posted form parameters
    errors  - a map of errors
           
### show ###

([control params] [form k params])

Generates the HTML for the control with the given key using the given posted parameters.
  
### show-controls ###

([form params errors] [form params errors format-fn])

Returns a string containing HTML for the controls on the given form.
Optionally takes a function (fn [label control-html error] ...)
that can be used to generate the html surrounding a control.
           
### validate ###

([form params])

Checks the values posted to the given controls.
Returns a two entry vector containing a map of validated values
and a map of error messages.

    params    - a map of parameter names to posted values
    form      - the form to check
           
