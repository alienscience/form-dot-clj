
# form-dot-clj #

A library for handling the display and validation of forms. Supports HTML5 forms, javascript validation and plain HTML.

## Example ##

    (def-field email-address
      [:email "Sorry, that style of email address is not supported"])

    (def-form example 
      {:size 20}
      :email (textbox email-address 
                      {:required "Please enter an email address"}))

    ;; Use the following function to show the form when generating html
    (show-controls example)

    ;; Use the following function to validate a post of this form
    (on-post example params success-fn failure-fn)
         

## Introduction ##

Forms: boring, easy to make small validation mistakes, sensible error reporting (example), don't loose input when errors reported. HTML5 and javascript validation. Server side validation still needed to protect against scripts.

Library that handles display, validation error reporting. Extensible - example using jquery tools to give server side validation, HTML5 form validation (when available), or javascript validation from one set of declarations.

Should be usable with any HTML templating system. If needed, provides fine grained control of the HTML that is generated.

## Walkthrough ##

Most websites need a signup form that can be used to register users. This is how we would use form-dot-clj to do it.

    (ns signup
      "Minimal signup form"
      (:use form-dot-clj)
      (:use form-dot-clj.html-controls))

The declaration above pulls in form-dot-clj and will give us access to HTML controls. If we wanted client side valiation via jquery tools we would use:

    (ns signup
      "Minimal signup form"
      (:use form-dot-clj)
      (:use form-dot-clj.jquery-tools))

Form-dot-clj uses controls and fields. A control is something that appears on a HTML page. A field is data that we will validate. Form-dot-clj separates these concepts as the same field may appear on different forms and be attached to different controls.

    (def-field username
      [:maxlength 20]
      [:pattern "[a-zA-Z]+" "Only alphanumeric characters please"]
      [:no-match #"(?i)(root|admin)" "Sorry that username is reserved"])

The declaration above creates a var called 'username' that is defined with a series of declarations in the format `[:check arg1 ... argn]`. The field is assumed to be a string. 

`[:maxlength 20]` sets the maximum length of the field. This declaration is used to produce HTML that limits the number of characters that can be input. The declaration is also used to build a check on the server.

`[:pattern "[a-zA-Z]+" "Error message"]` sets a, javascript compatible, regular expression that the text must fully match to be valid. This check also introduces an error message that will be displayed when the check fails. The check will be made on the serverside and optionally on the client if jquery tools is being used.

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

The HTML controls that use a size attribute will have that attribute set to 20 by default. All the controls will be required by default. If a textbox is not filled in, an error message saying "Required" will be displayed. In addition to checks created automatically from the fields we have specified our own checking function by setting `:check-fns`.

The controls for the form are each given with a key e.g:

    :username (textbox username)
    :password (textbox password {:type "password"})

The password box has the HTML type attribute set to "password". We want password and confirm-password to be the same value and we can enforce this by writing our own checking function:

    (defn confirm-password
      "Makes sure password and confirmation are the same"
      [params errors]
      (if-not (= (params "confirm-password")
                 (params "password"))
        {:password "Password and confirmation are not the same"}))

The checking function is passed a map of the parameters and a map of all the errors that have occured so far. The error map can be used to see if this check is needed at all - for instance we wouldn't want to do an expensive database check on a field if the field was already invalid. Errors are returned in a map with the format `:control-key "Error message` to indicate which control has an error.

We can now write a function to display the form. 
