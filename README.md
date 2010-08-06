
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

Generating and checking HTML forms is boring and its easy to make small mistakes -- for example, simply hit submit on an empty form and watch how too many web pages display unsuitable errors or even fail to spot input is missing.
-

 Improvements in form validation such as javascript form handling and HTML5 form handling improve response time for users but add to coding workload. Even when using client side validation, server checks are still needed to protect against automated scripts.

Form-dot-clj is a Clojure library that handles form display, validation, type conversion and error reporting. The library is extendable and an example is included where controls based on jquery tools (TODO link) are used to provide HTML5, javascript and server side validation from the same declarations.

This library should be usable with any HTML templating system and also provides fine grained control of the HTML that is generated.

## Walkthrough ##

Some websites need a signup form that can be used to register users. This is how we would use form-dot-clj to do it.

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

The declaration above creates a var called 'username' that is defined with a series of declarations in the format `[:check arg1 ... argn]`. By default, the field is assumed to be a string although other types are available. 

`[:maxlength 20]` sets the maximum length of the field. This declaration is used to produce HTML that limits the number of characters that can be input. The declaration is also used to build a check on the server.

`[:pattern "[a-zA-Z]+" "Error message"]` sets a, javascript compatible, regular expression that the text must fully match to be valid. This check also introduces an error message that will be displayed when the check fails. The check will be made on the serverside and optionally on the client if, for example, jquery tools is being used.

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
      (if-not (or (errors :password)
                  (= (params "confirm-password")
                     (params "password")))
        {:password "Password and confirmation are not the same"}))

The checking function is passed a map of the parameters and a map of all the errors that have occured so far. The error map can be used to see if this check is needed at all - for instance we wouldn't want to do an expensive database check on a field if the field was already invalid. Errors are returned in a map with the format `:control-key "Error message` to indicate which control has an error.

We can now write a function to display the form. 

    (defn show-form
      "The easy way to display a form"
      []
      (html
        [:form {:action "/signup" :method "post"}
           (show-controls signup)
           (default-submit "Sign Up")]))

The example above uses Hiccup but there is nothing to stop another HTML library being used. The function `(show-controls form)` returns the controls as HTML. The function `(default-submit "Label")` returns a HTML submit button with similar formatting. 

The HTML that is returned can be styled using CSS. However, if more control over the HTML is needed then it is possible to change the way each control is formatted or have full control over the form layout (TODO link).

Now, assuming we have a function that does the signup called `do-signup`, we need to write code to do validation and error display when the form is posted.

    (defn signup-post
      "The easy way to handle a post"
      [params]
      (on-post signup params do-signup show-form))

The `on-post` function takes as arguments the form to be validated, the posted parameters, a function to call on success and a function to display the form when errors occur.
      
If more control is needed when a form is posted then lowerlevel functions are available to handle the post in more detail (TODO link). The on-post function needs to be called during a post, for instance, when using Compojure:

    (defroutes myroutes
       (GET "/signup" [] (show-form))
       (POST "/signup" {params params}
          (signup-post params)))

To see how this fits together as a mini application, runnable examples are given in the 'examples' directory.
          
## Validation ##

The following validation tags are available for use with `def-field`

    [:maxlength x]
Sets the maximum character length for a field (client,server).

## API  ##

### form-dot-clj/bind-controls ###

    ([params errors body])
      Macro
       Binds the given parameter and error maps so they are available to
       form controls and then executes body.

### form-dot-clj/def-field ###

    ([field-name & validation])
      Macro
       Creates a var with the given name to hold a field definition.
       e.g
        (def-field username [:maxlength 20])  ;; creates (var username)

### form-dot-clj/def-form ###

    ([name options & definition])
      Macro
       Creates a var in the current namespace that contains
       the given form definition e.g
        (def-form login
          {:size 20}
          :username (textbox username)
          :password (textbox password {:type "password"}))

### form-dot-clj/default-submit ###

    ([label])
      The default way of displaying a submit button.

### form-dot-clj/on-error ###

     ([control error-fn] [form k error-fn])
       Executes error-fn if the given control has an error.
          form     - the form the control is on
          k        - the key of the control
          error-fn - (fn [error-message] ... )

### form-dot-clj/on-post ###

    ([form params success-fn fail-fn])
      Function that handles a form post.
      Executes success-fn on success, fail-fn on fail.
      The success-fn takes a single parameter containing a map of validated
      parameters.
      The fail-fn has no parameters.
           
### form-dot-clj/show ###

    ([control] [form k])
      Generates the HTML for the control with the given key.
  
### form-dot-clj/show-controls ###

    ([form] [form format-fn])
      Displays controls on the given form.
      Optionally takes a function (fn [label control] ...)
      that can be used to generate the html surrounding a control.
           
### form-dot-clj/validate ###

    ([form params])
      Checks the values posted to the given controls.
      Returns a two entry vector containing a map of validated values
      and a map of error messages.
       params    - a map of parameter names to posted values
       form      - the form to check
           
